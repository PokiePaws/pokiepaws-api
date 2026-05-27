package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.config.properties.VisitScheduleProperties;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import com.pokiepaws.api.services.ClinicQueryService;
import com.pokiepaws.api.services.ClinicService;
import java.time.LocalDate;
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
class ClinicServiceTest {

  @Mock ClinicRepository clinicRepository;
  @Mock VetRepository vetRepository;
  @Mock VisitRepository visitRepository;
  @Mock ClinicQueryService clinicQueryService;

  private ClinicService clinicService;

  @BeforeEach
  void setUp() {
    clinicService =
        new ClinicService(
            clinicRepository,
            vetRepository,
            visitRepository,
            new VisitScheduleProperties(),
            clinicQueryService);
  }

  @Test
  void getVetsByClinicId_shouldMapVetsForExistingClinic() {
    Clinic clinic = clinic(1L, "PokiePaws Legnica", "Legnica");
    Vet vet =
        Vet.builder()
            .userId(5L)
            .user(User.builder().email("vet@pokiepaws.pl").build())
            .clinic(clinic)
            .firstName("Jan")
            .lastName("Nowak")
            .npwz("1234567")
            .specialization("Chirurgia")
            .phone("+48123123123")
            .build();

    when(clinicQueryService.getByIdAsDto(1L)).thenReturn(clinic);
    when(vetRepository.findAllByClinicId(1L)).thenReturn(List.of(vet));

    var result = clinicService.getVetsByClinicId(1L);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(5L);
    assertThat(result.getFirst().getEmail()).isEqualTo("vet@pokiepaws.pl");
    assertThat(result.getFirst().getClinicName()).isEqualTo("PokiePaws Legnica");
  }

  @Test
  void getAvailableSlots_shouldExcludeTakenSlotsAndCancelledVisits() {
    LocalDate date = LocalDate.now().plusDays(1);

    Clinic clinic = clinic(1L, "PokiePaws Legnica", "Legnica");
    Vet vet = Vet.builder().userId(5L).clinic(clinic).build();

    Visit scheduled =
        Visit.builder()
            .startsAt(date.atTime(10, 0))
            .endsAt(date.atTime(10, 30))
            .status(VisitStatus.SCHEDULED)
            .build();

    Visit cancelled =
        Visit.builder()
            .startsAt(date.atTime(11, 0))
            .endsAt(date.atTime(11, 30))
            .status(VisitStatus.CANCELLED)
            .build();

    when(clinicQueryService.getByIdAsDto(1L)).thenReturn(clinic);
    when(vetRepository.findById(5L)).thenReturn(Optional.of(vet));
    when(visitRepository.findAllByVetUserIdAndStartsAtBetween(
            5L, date.atTime(9, 0), date.atTime(17, 0)))
        .thenReturn(List.of(scheduled, cancelled));

    var result = clinicService.getAvailableSlots(1L, 5L, date);

    assertThat(result.getAvailableStarts()).doesNotContain(date.atTime(10, 0));
    assertThat(result.getAvailableStarts()).contains(date.atTime(11, 0), date.atTime(16, 30));
  }

  @Test
  void getAvailableSlots_shouldThrow400_whenVetBelongsToDifferentClinic() {
    Clinic requestedClinic = clinic(1L, "PokiePaws Legnica", "Legnica");
    Vet vet = Vet.builder().userId(5L).clinic(clinic(2L, "Other", "Wroclaw")).build();

    when(clinicQueryService.getByIdAsDto(1L)).thenReturn(requestedClinic);
    when(vetRepository.findById(5L)).thenReturn(Optional.of(vet));

    LocalDate tomorrow = LocalDate.now().plusDays(1);

    assertThatThrownBy(() -> clinicService.getAvailableSlots(1L, 5L, tomorrow))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason())
                  .isEqualTo("Selected vet does not belong to selected clinic");
            });
  }

  @Test
  void delete_shouldDelegateToRepository() {
    clinicService.delete(7L);

    verify(clinicRepository).deleteById(7L);
  }

  private Clinic clinic(Long id, String name, String city) {
    return Clinic.builder()
        .id(id)
        .clinicName(name)
        .regon("123456789")
        .street("Zielona")
        .houseNumber("10")
        .postalCode("59-220")
        .city(city)
        .country("Poland")
        .phone("+48123123123")
        .email("clinic@pokiepaws.pl")
        .workingHours("09:00-17:00")
        .active(true)
        .build();
  }
}
