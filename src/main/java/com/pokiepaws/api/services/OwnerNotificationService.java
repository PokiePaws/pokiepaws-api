package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Prescription;
import com.pokiepaws.api.models.Visit;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class OwnerNotificationService {

  private final MobilePushNotificationService mobilePushNotificationService;
  private final OwnerEmailNotificationService ownerEmailNotificationService;

  public void visitConfirmed(Visit visit) {
    sendAfterCommit(
        () -> {
          mobilePushNotificationService.sendVisitConfirmed(visit);
          ownerEmailNotificationService.sendVisitConfirmed(visit);
        });
  }

  public void visitCancelled(Visit visit) {
    sendAfterCommit(
        () -> {
          mobilePushNotificationService.sendVisitCancelled(visit);
          ownerEmailNotificationService.sendVisitCancelled(visit);
        });
  }

  public void visitReminder(Visit visit, String reminderType) {
    sendAfterCommit(
        () -> {
          mobilePushNotificationService.sendVisitReminder(visit, reminderType);
          ownerEmailNotificationService.sendVisitReminder(visit);
        });
  }

  public void prescriptionCreated(Prescription prescription) {
    Visit visit = prescription.getVisit();
    sendAfterCommit(
        () -> {
          mobilePushNotificationService.sendPrescriptionCreated(prescription);
          ownerEmailNotificationService.sendPrescriptionCreated(visit);
        });
  }

  public void visitMedicalDataUpdated(Visit visit) {
    sendAfterCommit(
        () -> {
          mobilePushNotificationService.sendVisitMedicalDataUpdated(visit);
          ownerEmailNotificationService.sendVisitMedicalDataUpdated(visit);
        });
  }

  public void rabiesVaccinationReminder(Animal animal, LocalDate dueDate) {
    sendAfterCommit(
        () -> mobilePushNotificationService.sendRabiesVaccinationReminder(animal, dueDate));
  }

  private void sendAfterCommit(Runnable action) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      action.run();
      return;
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            action.run();
          }
        });
  }
}
