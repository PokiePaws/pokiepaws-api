package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.VisitRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.visit-reminders.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class VisitReminderScheduler {

  private static final List<VisitStatus> REMINDER_STATUSES =
      List.of(VisitStatus.SCHEDULED, VisitStatus.CONFIRMED);

  private final VisitRepository visitRepository;
  private final OwnerNotificationService ownerNotificationService;
  private final Clock clock;

  @Scheduled(fixedDelayString = "${app.visit-reminders.scan-delay-ms:60000}")
  public void sendVisitReminders() {
    LocalDateTime now = LocalDateTime.now(clock);
    send24HourReminders(now);
    send1HourReminders(now);
  }

  private void send24HourReminders(LocalDateTime now) {
    LocalDateTime from = now.plusHours(24).minusMinutes(1);
    LocalDateTime to = now.plusHours(24).plusMinutes(1);

    List<Visit> visits =
        visitRepository
            .findAllByStatusInAndReminder24hSentFalseAndStartsAtBetweenOrderByStartsAtAsc(
                REMINDER_STATUSES, from, to);

    for (Visit visit : visits) {
      ownerNotificationService.visitReminder(visit, "VISIT_REMINDER_24H");
      visit.setReminder24hSent(true);
      visitRepository.save(visit);
    }
  }

  private void send1HourReminders(LocalDateTime now) {
    LocalDateTime from = now.plusHours(1).minusMinutes(1);
    LocalDateTime to = now.plusHours(1).plusMinutes(1);

    List<Visit> visits =
        visitRepository.findAllByStatusInAndReminder1hSentFalseAndStartsAtBetweenOrderByStartsAtAsc(
            REMINDER_STATUSES, from, to);

    for (Visit visit : visits) {
      ownerNotificationService.visitReminder(visit, "VISIT_REMINDER_1H");
      visit.setReminder1hSent(true);
      visitRepository.save(visit);
    }
  }
}
