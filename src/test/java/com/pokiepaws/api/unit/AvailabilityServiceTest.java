package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.config.properties.VisitScheduleProperties;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VetWorkingHoursRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import com.pokiepaws.api.services.AvailabilityService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

  @Mock VetRepository vetRepository;
  @Mock VisitRepository visitRepository;
  @Mock VetWorkingHoursRepository vetWorkingHoursRepository;

  private AvailabilityService availabilityService;

  @BeforeEach
  void setUp() {
    availabilityService =
        new AvailabilityService(
            vetRepository, visitRepository, new VisitScheduleProperties(), vetWorkingHoursRepository);
  }

  @Test
  void getAvailableSlots_shouldGenerateThirtyMinuteSlotsAndExcludeScheduledVisits() {
    LocalDate date = LocalDate.of(2026, 5, 11);
    Clinic clinic = Clinic.builder().id(1L).clinicName("PokiePaws Legnica").build();
    Vet vet = Vet.builder().userId(5L).clinic(clinic).firstName("Jan").lastName("Nowak").build();
    Visit scheduledVisit =
        Visit.builder()
            .vet(vet)
            .startsAt(LocalDateTime.of(date, LocalTime.of(10, 0)))
            .endsAt(LocalDateTime.of(date, LocalTime.of(10, 30)))
            .status(VisitStatus.SCHEDULED)
            .build();
    Visit cancelledVisit =
        Visit.builder()
            .vet(vet)
            .startsAt(LocalDateTime.of(date, LocalTime.of(11, 0)))
            .endsAt(LocalDateTime.of(date, LocalTime.of(11, 30)))
            .status(VisitStatus.CANCELLED)
            .build();

    when(vetRepository.findById(5L)).thenReturn(Optional.of(vet));
    when(visitRepository.findAllByVetUserIdAndStartsAtBetween(
            5L, date.atTime(9, 0), date.atTime(17, 0)))
        .thenReturn(List.of(scheduledVisit, cancelledVisit));

    var response = availabilityService.getAvailableSlots(1L, 5L, date);

    assertThat(response.getClinicId()).isEqualTo(1L);
    assertThat(response.getVetUserId()).isEqualTo(5L);
    assertThat(response.getSlotMinutes()).isEqualTo(30);
    assertThat(response.getWorkdayStart()).isEqualTo(date.atTime(9, 0));
    assertThat(response.getWorkdayEnd()).isEqualTo(date.atTime(17, 0));
    assertThat(response.getAvailableStarts()).hasSize(15);
    assertThat(response.getAvailableStarts()).contains(date.atTime(9, 0), date.atTime(16, 30));
    assertThat(response.getAvailableStarts()).doesNotContain(date.atTime(10, 0));
    assertThat(response.getAvailableStarts()).contains(date.atTime(11, 0));
  }

  @Test
  void getAvailableSlots_shouldThrow404_whenVetDoesNotExist() {
    LocalDate testDate = LocalDate.of(2026, 5, 11);
    when(vetRepository.findById(404L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> availabilityService.getAvailableSlots(1L, 404L, testDate))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Vet not found");
            });
  }

  @Test
  void getAvailableSlots_shouldThrow400_whenVetDoesNotBelongToClinic() {
    LocalDate testDate = LocalDate.of(2026, 5, 11);
    Vet vet =
        Vet.builder()
            .userId(5L)
            .clinic(Clinic.builder().id(2L).clinicName("Other Clinic").build())
            .build();
    when(vetRepository.findById(5L)).thenReturn(Optional.of(vet));

    assertThatThrownBy(() -> availabilityService.getAvailableSlots(1L, 5L, testDate))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason())
                  .isEqualTo("Selected vet does not belong to selected clinic");
            });
  }
}
