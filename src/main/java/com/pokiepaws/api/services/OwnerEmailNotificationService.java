package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.Visit;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OwnerEmailNotificationService {

  private static final DateTimeFormatter VISIT_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private final JavaMailSender mailSender;

  @Value("${app.mail.from:no-reply@pokiepaws.local}")
  private String from;

  public void sendVisitConfirmed(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Visit Confirmed",
        "Your visit has been confirmed and scheduled for " + formatVisitTime(visit) + ".");
  }

  public void sendVisitCancelled(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Visit cancelled",
        "The visit is scheduled for " + formatVisitTime(visit) + " has been canceled.");
  }

  public void sendVisitCancelledByVet(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Wizyta odwolana przez weterynarza",
        "Weterynarz odwolal Twoja wizyte w dniu "
            + formatVisitTime(visit)
            + ". Prosimy o umowienie wizyty w innym terminie.");
  }

  public void sendVisitReminder(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Visit Reminder",
        "A reminder about your visit: " + formatVisitTime(visit) + ".");
  }

  public void sendPrescriptionCreated(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - New Prescription",
        "A prescription has been added to the appointment on " + formatVisitTime(visit) + ".");
  }

  public void sendVisitMedicalDataUpdated(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Animal medical info updated",
        "The medical data for the visit has been updated from " + formatVisitTime(visit) + ".");
  }

  private void sendVisitEmail(Visit visit, String subject, String body) {
    Owner owner = visit.getAnimal().getOwner();
    String to = owner.getUser().getEmail();

    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setFrom(from);
    msg.setTo(to);
    msg.setSubject(subject);
    msg.setText(body);

    mailSender.send(msg);
  }

  private String formatVisitTime(Visit visit) {
    return visit.getStartsAt().format(VISIT_TIME_FORMATTER);
  }
}
