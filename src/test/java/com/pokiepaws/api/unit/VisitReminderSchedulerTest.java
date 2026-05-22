package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.VisitRepository;
import com.pokiepaws.api.services.OwnerNotificationService;
import com.pokiepaws.api.services.VisitReminderScheduler;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VisitReminderSchedulerTest {

  @Mock VisitRepository visitRepository;
  @Mock OwnerNotificationService ownerNotificationService;

  private VisitReminderScheduler scheduler;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneId.of("UTC"));
    scheduler = new VisitReminderScheduler(visitRepository, ownerNotificationService, clock);
  }

  @Test
  void sendVisitReminders_shouldSendForScheduledAndConfirmedVisitsAndSaveAfterNotification() {
    Visit scheduled = Visit.builder().id(1L).status(VisitStatus.SCHEDULED).build();
    Visit confirmed = Visit.builder().id(2L).status(VisitStatus.CONFIRMED).build();
    LocalDateTime from24h = LocalDateTime.of(2026, 5, 11, 9, 59);
    LocalDateTime to24h = LocalDateTime.of(2026, 5, 11, 10, 1);
    LocalDateTime from1h = LocalDateTime.of(2026, 5, 10, 10, 59);
    LocalDateTime to1h = LocalDateTime.of(2026, 5, 10, 11, 1);

    when(visitRepository
            .findAllByStatusInAndReminder24hSentFalseAndStartsAtBetweenOrderByStartsAtAsc(
                any(), eq(from24h), eq(to24h)))
        .thenReturn(List.of(scheduled, confirmed));
    when(visitRepository
            .findAllByStatusInAndReminder1hSentFalseAndStartsAtBetweenOrderByStartsAtAsc(
                any(), eq(from1h), eq(to1h)))
        .thenReturn(List.of());

    scheduler.sendVisitReminders();

    assertThat(scheduled.isReminder24hSent()).isTrue();
    assertThat(confirmed.isReminder24hSent()).isTrue();
    verify(ownerNotificationService).visitReminder(scheduled, "VISIT_REMINDER_24H");
    verify(ownerNotificationService).visitReminder(confirmed, "VISIT_REMINDER_24H");
    verify(visitRepository).save(scheduled);
    verify(visitRepository).save(confirmed);

    ArgumentCaptor<Collection<VisitStatus>> statusesCaptor =
        ArgumentCaptor.forClass(Collection.class);
    verify(visitRepository)
        .findAllByStatusInAndReminder24hSentFalseAndStartsAtBetweenOrderByStartsAtAsc(
            statusesCaptor.capture(), eq(from24h), eq(to24h));
    assertThat(statusesCaptor.getValue())
        .containsExactlyInAnyOrder(VisitStatus.SCHEDULED, VisitStatus.CONFIRMED);
  }

  @Test
  void sendVisitReminders_shouldNotMarkReminderSent_whenNotificationFails() {
    Visit visit = Visit.builder().id(1L).status(VisitStatus.CONFIRMED).build();
    LocalDateTime from24h = LocalDateTime.of(2026, 5, 11, 9, 59);
    LocalDateTime to24h = LocalDateTime.of(2026, 5, 11, 10, 1);

    when(visitRepository
            .findAllByStatusInAndReminder24hSentFalseAndStartsAtBetweenOrderByStartsAtAsc(
                any(), eq(from24h), eq(to24h)))
        .thenReturn(List.of(visit));
    doThrow(new RuntimeException("mail failed"))
        .when(ownerNotificationService)
        .visitReminder(visit, "VISIT_REMINDER_24H");

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> scheduler.sendVisitReminders())
        .isInstanceOf(RuntimeException.class)
        .hasMessage("mail failed");

    assertThat(visit.isReminder24hSent()).isFalse();
    verify(visitRepository, never()).save(visit);
  }
}
