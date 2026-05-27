package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.pokiepaws.api.dto.laborder.CreateLabOrderRequest;
import com.pokiepaws.api.dto.laborder.LabOrderResponse;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import com.pokiepaws.api.services.LabOrderService;
import com.pokiepaws.api.services.LabOrderWarehouseIntegrationService;
import com.pokiepaws.api.services.RealtimeNotificationService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class LabOrderServiceTest {

  @Mock LabOrderRepository labOrderRepository;
  @Mock LabOrderStatusHistoryRepository statusHistoryRepository;
  @Mock AnimalRepository animalRepository;
  @Mock VisitRepository visitRepository;
  @Mock VetRepository vetRepository;
  @Mock UserRepository userRepository;
  @Mock RealtimeNotificationService realtimeNotificationService;
  @Mock LabOrderWarehouseIntegrationService warehouseIntegrationService;

  private LabOrderService labOrderService;

  private static final String VET_EMAIL = "jan.kowalski@pokiepaws.pl";
  private User vetUser;
  private Vet vet;
  private Clinic clinic;
  private Animal animal;

  @BeforeEach
  void setUp() {
    labOrderService =
        new LabOrderService(
            labOrderRepository,
            statusHistoryRepository,
            animalRepository,
            visitRepository,
            vetRepository,
            userRepository,
            realtimeNotificationService,
            warehouseIntegrationService);

    clinic = Clinic.builder().id(1L).clinicName("PokiePaws Klinika").build();
    vetUser = User.builder().id(2L).email(VET_EMAIL).build();
    vet =
        Vet.builder()
            .userId(2L)
            .user(vetUser)
            .firstName("Jan")
            .lastName("Kowalski")
            .clinic(clinic)
            .build();
    animal = Animal.builder().id(3L).name("Burek").species("Pies").build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Nested
  class CreateForAnimal {

    @Test
    void success_createsLabOrderAndRecordsInitialHistory() {
      authenticate();
      CreateLabOrderRequest request = validRequest();

      when(animalRepository.findById(3L)).thenReturn(Optional.of(animal));
      when(userRepository.findByEmail(VET_EMAIL)).thenReturn(Optional.of(vetUser));
      when(vetRepository.findByUserEmail(VET_EMAIL)).thenReturn(Optional.of(vet));
      when(labOrderRepository.save(any(LabOrder.class)))
          .thenAnswer(
              inv -> {
                LabOrder lo = inv.getArgument(0);
                lo.setId(10L);
                return lo;
              });
      when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
      when(statusHistoryRepository.findAllByLabOrderIdOrderByChangedAtAsc(10L))
          .thenReturn(List.of());

      LabOrderResponse result = labOrderService.createForAnimal(3L, request);

      assertThat(result).isNotNull();
      assertThat(result.getTestType()).isEqualTo("Morfologia krwi (CBC)");
      assertThat(result.getStatus()).isEqualTo(LabOrderStatus.PENDING);
      assertThat(result.getAnimalId()).isEqualTo(3L);
      assertThat(result.getClinicId()).isEqualTo(1L);

      verify(labOrderRepository).save(any(LabOrder.class));
      verify(statusHistoryRepository).save(any(LabOrderStatusHistory.class));
      verify(realtimeNotificationService).publishLabOrderCreated(any(LabOrder.class));
      // No active Spring transaction in unit test → warehouse called synchronously
      verify(warehouseIntegrationService)
          .createWarehouseOrder(eq(10L), eq(1L), eq("Morfologia krwi (CBC)"));
    }

    @Test
    void throwsNotFound_whenAnimalDoesNotExist() {
      authenticate();
      when(animalRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> labOrderService.createForAnimal(99L, validRequest()))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex ->
                  assertThat(((ResponseStatusException) ex).getStatusCode())
                      .isEqualTo(HttpStatus.NOT_FOUND));

      verify(labOrderRepository, never()).save(any());
    }

    @Test
    void throwsNotFound_whenVetDoesNotExist() {
      authenticate();
      when(animalRepository.findById(3L)).thenReturn(Optional.of(animal));
      when(userRepository.findByEmail(VET_EMAIL)).thenReturn(Optional.of(vetUser));
      when(vetRepository.findByUserEmail(VET_EMAIL)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> labOrderService.createForAnimal(3L, validRequest()))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex ->
                  assertThat(((ResponseStatusException) ex).getStatusCode())
                      .isEqualTo(HttpStatus.NOT_FOUND));

      verify(labOrderRepository, never()).save(any());
    }

    @Test
    void throwsBadRequest_whenVetHasNoClinic() {
      authenticate();
      Vet vetWithoutClinic =
          Vet.builder().userId(2L).user(vetUser).firstName("Jan").lastName("Kowalski").build();

      when(animalRepository.findById(3L)).thenReturn(Optional.of(animal));
      when(userRepository.findByEmail(VET_EMAIL)).thenReturn(Optional.of(vetUser));
      when(vetRepository.findByUserEmail(VET_EMAIL)).thenReturn(Optional.of(vetWithoutClinic));

      assertThatThrownBy(() -> labOrderService.createForAnimal(3L, validRequest()))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex -> {
                ResponseStatusException rse = (ResponseStatusException) ex;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(rse.getReason()).contains("clinic");
              });

      verify(labOrderRepository, never()).save(any());
    }
  }

  @Nested
  class UpdateStatus {

    @Test
    void pendingToConfirmed_succeeds() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.PENDING);

      stubForStatusUpdate(lo);

      LabOrderResponse result = labOrderService.updateStatus(10L, LabOrderStatus.CONFIRMED);

      assertThat(result.getStatus()).isEqualTo(LabOrderStatus.CONFIRMED);
      assertThat(lo.getCompletedAt()).isNull();
      verify(statusHistoryRepository).save(any(LabOrderStatusHistory.class));
      verify(realtimeNotificationService).publishLabOrderStatusUpdated(any(LabOrder.class));
    }

    @Test
    void confirmedToInProgress_succeeds() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.CONFIRMED);

      stubForStatusUpdate(lo);

      LabOrderResponse result = labOrderService.updateStatus(10L, LabOrderStatus.IN_PROGRESS);

      assertThat(result.getStatus()).isEqualTo(LabOrderStatus.IN_PROGRESS);
    }

    @Test
    void inProgressToCompleted_setsCompletedAt() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.IN_PROGRESS);

      stubForStatusUpdate(lo);

      LabOrderResponse result = labOrderService.updateStatus(10L, LabOrderStatus.COMPLETED);

      assertThat(result.getStatus()).isEqualTo(LabOrderStatus.COMPLETED);
      assertThat(lo.getCompletedAt()).isNotNull();
    }

    @Test
    void pendingToCancelled_setsCompletedAt() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.PENDING);

      stubForStatusUpdate(lo);

      LabOrderResponse result = labOrderService.updateStatus(10L, LabOrderStatus.CANCELLED);

      assertThat(result.getStatus()).isEqualTo(LabOrderStatus.CANCELLED);
      assertThat(lo.getCompletedAt()).isNotNull();
    }

    @Test
    void throwsBadRequest_pendingDirectlyToInProgress() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.PENDING);
      when(labOrderRepository.findById(10L)).thenReturn(Optional.of(lo));

      assertThatThrownBy(() -> labOrderService.updateStatus(10L, LabOrderStatus.IN_PROGRESS))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex -> {
                ResponseStatusException rse = (ResponseStatusException) ex;
                assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(rse.getReason()).contains("Invalid status transition");
              });

      verify(labOrderRepository, never()).save(any());
    }

    @Test
    void throwsBadRequest_completedToAnyStatus() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.COMPLETED);
      when(labOrderRepository.findById(10L)).thenReturn(Optional.of(lo));

      assertThatThrownBy(() -> labOrderService.updateStatus(10L, LabOrderStatus.PENDING))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex ->
                  assertThat(((ResponseStatusException) ex).getStatusCode())
                      .isEqualTo(HttpStatus.BAD_REQUEST));

      verify(labOrderRepository, never()).save(any());
    }

    @Test
    void throwsBadRequest_cancelledToAnyStatus() {
      authenticate();
      LabOrder lo = labOrder(10L, LabOrderStatus.CANCELLED);
      when(labOrderRepository.findById(10L)).thenReturn(Optional.of(lo));

      assertThatThrownBy(() -> labOrderService.updateStatus(10L, LabOrderStatus.CONFIRMED))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex ->
                  assertThat(((ResponseStatusException) ex).getStatusCode())
                      .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void throwsNotFound_whenLabOrderDoesNotExist() {
      authenticate();
      when(labOrderRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> labOrderService.updateStatus(99L, LabOrderStatus.CONFIRMED))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex ->
                  assertThat(((ResponseStatusException) ex).getStatusCode())
                      .isEqualTo(HttpStatus.NOT_FOUND));
    }
  }

  @Nested
  class Queries {

    @Test
    void getByClinic_returnsMappedList() {
      LabOrder lo = labOrder(1L, LabOrderStatus.PENDING);
      when(labOrderRepository.findAllByClinicIdOrderByOrderedAtDesc(1L)).thenReturn(List.of(lo));
      when(statusHistoryRepository.findAllByLabOrderIdOrderByChangedAtAsc(1L))
          .thenReturn(List.of());

      List<LabOrderResponse> result = labOrderService.getByClinic(1L);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getStatus()).isEqualTo(LabOrderStatus.PENDING);
      assertThat(result.getFirst().getStatusHistory()).isEmpty();
    }

    @Test
    void getByAnimal_returnsMappedList() {
      LabOrder lo = labOrder(2L, LabOrderStatus.IN_PROGRESS);
      when(labOrderRepository.findAllByAnimalIdOrderByOrderedAtDesc(3L)).thenReturn(List.of(lo));
      when(statusHistoryRepository.findAllByLabOrderIdOrderByChangedAtAsc(2L))
          .thenReturn(List.of());

      List<LabOrderResponse> result = labOrderService.getByAnimal(3L);

      assertThat(result).hasSize(1);
      assertThat(result.getFirst().getStatus()).isEqualTo(LabOrderStatus.IN_PROGRESS);
    }

    @Test
    void getByClinic_returnsEmptyList_whenNoneExist() {
      when(labOrderRepository.findAllByClinicIdOrderByOrderedAtDesc(99L)).thenReturn(List.of());

      List<LabOrderResponse> result = labOrderService.getByClinic(99L);

      assertThat(result).isEmpty();
    }
  }

  private void authenticate() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(VET_EMAIL, "secret", List.of()));
  }

  private CreateLabOrderRequest validRequest() {
    CreateLabOrderRequest r = new CreateLabOrderRequest();
    r.setTestType("Morfologia krwi (CBC)");
    r.setPriority(LabOrderPriority.NORMAL);
    return r;
  }

  private LabOrder labOrder(Long id, LabOrderStatus status) {
    return LabOrder.builder()
        .id(id)
        .animal(animal)
        .vet(vet)
        .clinic(clinic)
        .testType("Morfologia krwi")
        .priority(LabOrderPriority.NORMAL)
        .status(status)
        .build();
  }

  private void stubForStatusUpdate(LabOrder lo) {
    when(labOrderRepository.findById(lo.getId())).thenReturn(Optional.of(lo));
    when(userRepository.findByEmail(VET_EMAIL)).thenReturn(Optional.of(vetUser));
    when(statusHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(labOrderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(statusHistoryRepository.findAllByLabOrderIdOrderByChangedAtAsc(lo.getId()))
        .thenReturn(List.of());
  }
}
