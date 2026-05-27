package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.dto.vet.VetRequest;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.services.VetService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VetServiceTest {

  @Mock VetRepository vetRepository;
  @Mock ClinicRepository clinicRepository;
  @Mock UserRepository userRepository;

  private VetService vetService;
  private Clinic clinic;

  @BeforeEach
  void setUp() {
    vetService = new VetService(vetRepository, clinicRepository, userRepository);
    clinic = Clinic.builder().id(1L).clinicName("PokiePaws Legnica").build();
  }

  @Test
  void save_shouldCreateVetAssignedToClinic() {
    VetRequest request = vetRequest();
    when(clinicRepository.findById(1L)).thenReturn(Optional.of(clinic));
    when(vetRepository.save(any(Vet.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Vet result = vetService.save(request);

    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getClinic()).isEqualTo(clinic);

    ArgumentCaptor<Vet> vetCaptor = ArgumentCaptor.forClass(Vet.class);
    verify(vetRepository).save(vetCaptor.capture());
    assertThat(vetCaptor.getValue().getNpwz()).isEqualTo("1234567");
  }

  @Test
  void save_shouldThrowEntityNotFound_whenClinicDoesNotExist() {
    VetRequest request = vetRequest();
    when(clinicRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vetService.save(request))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Clinic not found");
  }

  @Test
  void update_shouldChangeVetDataAndClinic() {
    Vet existing =
        Vet.builder().userId(5L).firstName("Old").lastName("Name").clinic(clinic).build();
    Clinic newClinic = Clinic.builder().id(2L).clinicName("PokiePaws Wroclaw").build();
    VetRequest request = vetRequest();
    request.setClinicId(2L);
    request.setSpecialization("Dermatology");

    when(vetRepository.findById(5L)).thenReturn(Optional.of(existing));
    when(clinicRepository.findById(2L)).thenReturn(Optional.of(newClinic));
    when(vetRepository.save(existing)).thenReturn(existing);

    Vet result = vetService.update(5L, request);

    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getSpecialization()).isEqualTo("Dermatology");
    assertThat(result.getClinic()).isEqualTo(newClinic);
    verify(vetRepository).save(existing);
  }

  @Test
  void delete_shouldDeleteVetAndDeactivateLinkedUser() {
    User user =
        User.builder()
            .id(5L)
            .email("vet@pokiepaws.pl")
            .role(Role.VET)
            .active(true)
            .emailVerified(true)
            .build();
    Vet vet = Vet.builder().userId(5L).user(user).firstName("John").lastName("Smith").build();
    when(vetRepository.findById(5L)).thenReturn(Optional.of(vet));

    vetService.delete(5L);

    verify(vetRepository).delete(vet);
    assertThat(user.isActive()).isFalse();
    verify(userRepository).save(user);
  }

  @Test
  void getListItemsByClinic_shouldReturnCompactVetDtos() {
    Vet vet =
        Vet.builder()
            .userId(5L)
            .firstName("John")
            .lastName("Smith")
            .npwz("1234567")
            .specialization("Surgery")
            .build();
    when(vetRepository.findAllByClinicId(1L)).thenReturn(List.of(vet));

    var result = vetService.getListItemsByClinic(1L);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getUserId()).isEqualTo(5L);
    assertThat(result.getFirst().getSpecialization()).isEqualTo("Surgery");
  }

  private VetRequest vetRequest() {
    VetRequest request = new VetRequest();
    request.setFirstName("John");
    request.setLastName("Smith");
    request.setPhone("+48123123123");
    request.setNpwz("1234567");
    request.setSpecialization("Surgery");
    request.setClinicId(1L);
    return request;
  }
}
