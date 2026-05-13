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
        "PokiePaws - Wizyta potwierdzona",
        "Twoja wizyta zostala potwierdzona i zaplanowana na " + formatVisitTime(visit) + ".");
  }

  public void sendVisitCancelled(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Wizyta odwolana",
        "Wizyta zaplanowana na " + formatVisitTime(visit) + " zostala odwolana.");
  }

  public void sendVisitReminder(Visit visit, String reminderType) {
    sendVisitEmail(
        visit,
        "PokiePaws - Przypomnienie o wizycie",
        "Przypominamy o wizycie: " + formatVisitTime(visit) + ".");
  }

  public void sendPrescriptionCreated(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Nowa recepta",
        "Do wizyty z " + formatVisitTime(visit) + " dodano recepte.");
  }

  public void sendVisitMedicalDataUpdated(Visit visit) {
    sendVisitEmail(
        visit,
        "PokiePaws - Zaktualizowano dane medyczne",
        "Zaktualizowano dane medyczne wizyty z " + formatVisitTime(visit) + ".");
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
