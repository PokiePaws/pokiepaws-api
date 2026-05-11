package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VisitRepository;
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

  private ClinicService clinicService;

  @BeforeEach
  void setUp() {
    clinicService = new ClinicService(clinicRepository, vetRepository, visitRepository);
  }

  @Test
  void getAllAsDto_shouldReturnOnlyActiveClinics() {
    Clinic active = clinic(1L, "PokiePaws Legnica", "Legnica", true);
    Clinic inactive = clinic(2L, "PokiePaws Wroclaw", "Wroclaw", false);
    when(clinicRepository.findAll()).thenReturn(List.of(active, inactive));

    var result = clinicService.getAllAsDto();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(1L);
    assertThat(result.getFirst().getClinicName()).isEqualTo("PokiePaws Legnica");
    assertThat(result.getFirst().getCity()).isEqualTo("Legnica");
  }

  @Test
  void getByCityAsDto_shouldReturnOnlyActiveClinicsInCity() {
    Clinic active = clinic(1L, "PokiePaws Legnica", "Legnica", true);
    Clinic inactive = clinic(2L, "Closed PokiePaws", "Legnica", false);
    when(clinicRepository.findAllByCity("Legnica")).thenReturn(List.of(active, inactive));

    var result = clinicService.getByCityAsDto("Legnica");

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getClinicName()).isEqualTo("PokiePaws Legnica");
  }

  @Test
  void getByIdAsDto_shouldThrow404_whenClinicDoesNotExist() {
    when(clinicRepository.findById(404L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> clinicService.getByIdAsDto(404L))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Clinic not found");
            });
  }

  @Test
  void getVetsByClinicId_shouldMapVetsForExistingClinic() {
    Clinic clinic = clinic(1L, "PokiePaws Legnica", "Legnica", true);
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

    when(clinicRepository.findById(1L)).thenReturn(Optional.of(clinic));
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
    Clinic clinic = clinic(1L, "PokiePaws Legnica", "Legnica", true);
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

    when(clinicRepository.findById(1L)).thenReturn(Optional.of(clinic));
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
    Clinic requestedClinic = clinic(1L, "PokiePaws Legnica", "Legnica", true);
    Vet vet = Vet.builder().userId(5L).clinic(clinic(2L, "Other", "Wroclaw", true)).build();

    when(clinicRepository.findById(1L)).thenReturn(Optional.of(requestedClinic));
    when(vetRepository.findById(5L)).thenReturn(Optional.of(vet));

    LocalDate tomorrow = LocalDate.now().plusDays(1);

    assertThatThrownBy(() -> clinicService.getAvailableSlots(1L, 5L, tomorrow))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("Vet does not belong to this clinic");
            });
  }

  @Test
  void delete_shouldDelegateToRepository() {
    clinicService.delete(7L);

    verify(clinicRepository).deleteById(7L);
  }

  private Clinic clinic(Long id, String name, String city, boolean active) {
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
        .active(active)
        .build();
  }
}
