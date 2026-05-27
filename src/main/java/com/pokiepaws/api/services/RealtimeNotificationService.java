package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.auth.ActivityLogResponse;
import com.pokiepaws.api.dto.realtime.RealtimeEventType;
import com.pokiepaws.api.dto.realtime.RealtimeNotification;
import com.pokiepaws.api.models.ClinicStockItem;
import com.pokiepaws.api.models.LabOrder;
import com.pokiepaws.api.models.Prescription;
import com.pokiepaws.api.models.Visit;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class RealtimeNotificationService {

  private static final String USER_NOTIFICATIONS_QUEUE = "/queue/notifications";
  private static final String ADMIN_ACTIVITY_TOPIC = "/topic/admin/activity";
  private static final String TOPIC_VISITS = "visits";
  private static final String KEY_CLINIC_ID = "clinicId";
  private static final String KEY_ANIMAL_ID = "animalId";
  private static final String KEY_STARTS_AT = "startsAt";

  private final SimpMessagingTemplate messagingTemplate;
  private final Clock clock;

  public void publishVisitCreated(Visit visit) {
    String vetEmail = visit.getVet().getUser().getEmail();
    Long clinicId = visit.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.VISIT_CREATED,
            visit.getId(),
            "New visit scheduled",
            Map.of(
                KEY_CLINIC_ID, clinicId,
                KEY_ANIMAL_ID, visit.getAnimal().getId(),
                KEY_STARTS_AT, visit.getStartsAt()));

    sendAfterCommit(
        () -> {
          sendToUser(vetEmail, notification);
          sendToClinic(clinicId, TOPIC_VISITS, notification);
        });
  }

  public void publishVisitCancelled(Visit visit) {
    String vetEmail = visit.getVet().getUser().getEmail();
    Long clinicId = visit.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.VISIT_CANCELLED,
            visit.getId(),
            "Visit cancelled",
            Map.of(
                KEY_CLINIC_ID, clinicId,
                KEY_ANIMAL_ID, visit.getAnimal().getId(),
                KEY_STARTS_AT, visit.getStartsAt()));

    sendAfterCommit(
        () -> {
          sendToUser(vetEmail, notification);
          sendToClinic(clinicId, TOPIC_VISITS, notification);
        });
  }

  public void publishVisitMedicalDataUpdated(Visit visit) {
    Long clinicId = visit.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.VISIT_MEDICAL_DATA_UPDATED,
            visit.getId(),
            "Visit medical data updated",
            Map.of(
                KEY_CLINIC_ID, clinicId,
                KEY_ANIMAL_ID, visit.getAnimal().getId(),
                KEY_STARTS_AT, visit.getStartsAt()));

    sendAfterCommit(() -> sendToClinic(clinicId, TOPIC_VISITS, notification));
  }

  public void publishPrescriptionCreated(Prescription prescription) {
    Visit visit = prescription.getVisit();
    Long clinicId = prescription.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.PRESCRIPTION_CREATED,
            prescription.getId(),
            "Prescription created",
            Map.of(
                "visitId",
                visit.getId(),
                KEY_CLINIC_ID,
                clinicId,
                "vetUserId",
                prescription.getVet().getUserId()));

    sendAfterCommit(() -> sendToClinic(clinicId, TOPIC_VISITS, notification));
  }

  public void publishClinicStockUpdated(ClinicStockItem stockItem) {
    Long clinicId = stockItem.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.CLINIC_STOCK_UPDATED,
            stockItem.getId(),
            "Clinic stock updated",
            Map.of(
                KEY_CLINIC_ID,
                clinicId,
                "productId",
                stockItem.getProduct().getId(),
                "quantityPackages",
                stockItem.getQuantityPackages()));

    sendAfterCommit(
        () -> {
          sendToClinic(clinicId, "stock", notification);
          messagingTemplate.convertAndSend("/topic/warehouse/stock", notification);
        });
  }

  public void publishLabOrderCreated(LabOrder labOrder) {
    Long clinicId = labOrder.getClinic().getId();
    String vetEmail = labOrder.getVet().getUser().getEmail();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.LAB_ORDER_CREATED,
            labOrder.getId(),
            "Lab order created",
            Map.of(
                KEY_CLINIC_ID,
                clinicId,
                KEY_ANIMAL_ID,
                labOrder.getAnimal().getId(),
                "testType",
                labOrder.getTestType(),
                "priority",
                labOrder.getPriority().name()));

    sendAfterCommit(
        () -> {
          sendToUser(vetEmail, notification);
          sendToClinic(clinicId, "lab-orders", notification);
        });
  }

  public void publishLabOrderStatusUpdated(LabOrder labOrder) {
    Long clinicId = labOrder.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.LAB_ORDER_STATUS_UPDATED,
            labOrder.getId(),
            "Lab order status updated",
            Map.of(
                KEY_CLINIC_ID,
                clinicId,
                KEY_ANIMAL_ID,
                labOrder.getAnimal().getId(),
                "status",
                labOrder.getStatus().name()));

    sendAfterCommit(() -> sendToClinic(clinicId, "lab-orders", notification));
  }

  public void publishActivityLogCreated(ActivityLogResponse activityLog) {
    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.ACTIVITY_LOG_CREATED,
            activityLog.getId(),
            "Activity log created",
            Map.of(
                "type", activityLog.getType(),
                "userEmail", activityLog.getUserEmail(),
                "detail", activityLog.getDetail(),
                "clinic", activityLog.getClinic() == null ? "" : activityLog.getClinic()));

    sendAfterCommit(() -> messagingTemplate.convertAndSend(ADMIN_ACTIVITY_TOPIC, notification));
  }

  private RealtimeNotification buildNotification(
      RealtimeEventType type, Long entityId, String message, Map<String, Object> details) {
    return RealtimeNotification.builder()
        .type(type)
        .entityId(entityId)
        .message(message)
        .createdAt(LocalDateTime.now(clock))
        .details(details)
        .build();
  }

  private void sendToUser(String userEmail, RealtimeNotification notification) {
    messagingTemplate.convertAndSendToUser(userEmail, USER_NOTIFICATIONS_QUEUE, notification);
  }

  private void sendToClinic(Long clinicId, String topicName, RealtimeNotification notification) {
    messagingTemplate.convertAndSend("/topic/clinics/" + clinicId + "/" + topicName, notification);
  }

  private void sendAfterCommit(Runnable sendNotification) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      sendNotification.run();
      return;
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            sendNotification.run();
          }
        });
  }
}
