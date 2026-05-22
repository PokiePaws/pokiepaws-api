package com.pokiepaws.api.services;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.pokiepaws.api.models.OwnerDeviceToken;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.repositories.OwnerDeviceTokenRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobilePushNotificationService {

  private static final DateTimeFormatter VISIT_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private final OwnerDeviceTokenRepository ownerDeviceTokenRepository;

  public void sendVisitConfirmed(Visit visit) {
    sendVisitNotificationAfterCommit(
        visit,
        "Wizyta potwierdzona",
        "Twoja wizyta zostala zaplanowana na " + formatVisitTime(visit) + ".",
        "VISIT_CONFIRMED");
  }

  public void sendVisitCancelled(Visit visit) {
    sendVisitNotificationAfterCommit(
        visit,
        "Wizyta odwolana",
        "Wizyta zaplanowana na " + formatVisitTime(visit) + " zostala odwolana.",
        "VISIT_CANCELLED");
  }

  public void sendVisitReminder(Visit visit, String reminderType) {
    sendVisitNotificationAfterCommit(
        visit,
        "Przypomnienie o wizycie",
        "Przypominamy o wizycie: " + formatVisitTime(visit) + ".",
        reminderType);
  }

  public void sendPrescriptionCreated(Visit visit) {
    sendVisitNotificationAfterCommit(
        visit,
        "Nowa recepta",
        "Do wizyty z " + formatVisitTime(visit) + " dodano recepte.",
        "PRESCRIPTION_CREATED");
  }

  public void sendVisitMedicalDataUpdated(Visit visit) {
    sendVisitNotificationAfterCommit(
        visit,
        "Dane medyczne zaktualizowane",
        "Zaktualizowano dane medyczne wizyty z " + formatVisitTime(visit) + ".",
        "VISIT_MEDICAL_DATA_UPDATED");
  }

  private void sendVisitNotificationAfterCommit(
      Visit visit, String title, String body, String eventType) {
    Long ownerUserId = visit.getAnimal().getOwner().getUserId();
    Long visitId = visit.getId();

    sendToOwner(
        ownerUserId,
        title,
        body,
        Map.of(
            "type", eventType,
            "visitId", String.valueOf(visitId),
            "startsAt", visit.getStartsAt().toString()));
  }

  private void sendToOwner(Long ownerUserId, String title, String body, Map<String, String> data) {
    List<OwnerDeviceToken> deviceTokens =
        ownerDeviceTokenRepository.findAllByOwnerUserId(ownerUserId);
    if (deviceTokens.isEmpty()) {
      return;
    }

    List<String> tokens = deviceTokens.stream().map(OwnerDeviceToken::getToken).toList();
    MulticastMessage message =
        MulticastMessage.builder()
            .addAllTokens(tokens)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
            .putAllData(data)
            .build();

    try {
      BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
      log.info(
          "Firebase push notification sent for ownerUserId={}. successCount={}, failureCount={}",
          ownerUserId,
          response.getSuccessCount(),
          response.getFailureCount());
      removeInvalidTokens(deviceTokens, response);
    } catch (IllegalStateException ex) {
      log.warn("Firebase Admin SDK is not configured. Skipping mobile push notification.");
    } catch (FirebaseMessagingException ex) {
      log.warn(
          "Could not send Firebase push notification for ownerUserId={}: {}",
          ownerUserId,
          ex.getMessage());
    }
  }

  private void removeInvalidTokens(List<OwnerDeviceToken> deviceTokens, BatchResponse response) {
    List<SendResponse> responses = response.getResponses();
    for (int i = 0; i < responses.size(); i++) {
      SendResponse sendResponse = responses.get(i);
      if (!sendResponse.isSuccessful() && isInvalidTokenResponse(sendResponse)) {
        ownerDeviceTokenRepository.delete(deviceTokens.get(i));
      }
    }
  }

  private boolean isInvalidTokenResponse(SendResponse sendResponse) {
    FirebaseMessagingException exception = sendResponse.getException();
    return exception != null
        && (MessagingErrorCode.UNREGISTERED.equals(exception.getMessagingErrorCode())
            || MessagingErrorCode.INVALID_ARGUMENT.equals(exception.getMessagingErrorCode()));
  }

  private String formatVisitTime(Visit visit) {
    return visit.getStartsAt().format(VISIT_TIME_FORMATTER);
  }

}
