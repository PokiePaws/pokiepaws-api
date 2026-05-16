package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.auth.ActivityLogResponse;
import com.pokiepaws.api.dto.realtime.RealtimeEventType;
import com.pokiepaws.api.dto.realtime.RealtimeNotification;
import com.pokiepaws.api.models.ClinicStockItem;
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
                "clinicId", clinicId,
                "animalId", visit.getAnimal().getId(),
                "startsAt", visit.getStartsAt()));

    sendAfterCommit(
        () -> {
          sendToUser(vetEmail, notification);
          sendToClinic(clinicId, "visits", notification);
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
                "clinicId", clinicId,
                "animalId", visit.getAnimal().getId(),
                "startsAt", visit.getStartsAt()));

    sendAfterCommit(
        () -> {
          sendToUser(vetEmail, notification);
          sendToClinic(clinicId, "visits", notification);
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
                "clinicId", clinicId,
                "animalId", visit.getAnimal().getId(),
                "startsAt", visit.getStartsAt()));

    sendAfterCommit(() -> sendToClinic(clinicId, "visits", notification));
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
                "visitId", visit.getId(),
                "clinicId", clinicId,
                "vetUserId", prescription.getVet().getUserId()));

    sendAfterCommit(() -> sendToClinic(clinicId, "visits", notification));
  }

  public void publishClinicStockUpdated(ClinicStockItem stockItem) {
    Long clinicId = stockItem.getClinic().getId();

    RealtimeNotification notification =
        buildNotification(
            RealtimeEventType.CLINIC_STOCK_UPDATED,
            stockItem.getId(),
            "Clinic stock updated",
            Map.of(
                "clinicId", clinicId,
                "productId", stockItem.getProduct().getId(),
                "quantityPackages", stockItem.getQuantityPackages()));

    sendAfterCommit(
        () -> {
          sendToClinic(clinicId, "stock", notification);
          messagingTemplate.convertAndSend("/topic/warehouse/stock", notification);
        });
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
