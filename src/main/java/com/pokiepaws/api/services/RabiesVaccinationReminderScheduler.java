package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.repositories.AnimalRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.vaccination-reminders.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class RabiesVaccinationReminderScheduler {

  private final AnimalRepository animalRepository;
  private final OwnerNotificationService ownerNotificationService;
  private final Clock clock;

  @Value("${app.vaccination-reminders.rabies-valid-years:1}")
  private int rabiesValidYears;

  @Value("${app.vaccination-reminders.rabies-remind-before-days:30}")
  private int rabiesRemindBeforeDays;

  @Scheduled(fixedDelayString = "${app.vaccination-reminders.scan-delay-ms:3600000}")
  @Transactional
  public void sendRabiesVaccinationReminders() {
    LocalDate today = LocalDate.now(clock);
    LocalDate latestVaccinationDateForReminder =
        today.plusDays(rabiesRemindBeforeDays).minusYears(rabiesValidYears);

    List<Animal> animals =
        animalRepository
            .findAllByActiveTrueAndRabiesVaccinationDateIsNotNullAndRabiesVaccinationReminderSentFalseAndRabiesVaccinationDateLessThanEqual(
                latestVaccinationDateForReminder);

    for (Animal animal : animals) {
      LocalDate dueDate = animal.getRabiesVaccinationDate().plusYears(rabiesValidYears);
      ownerNotificationService.rabiesVaccinationReminder(animal, dueDate);
      animal.setRabiesVaccinationReminderSent(true);
      animalRepository.save(animal);
    }
  }
}
