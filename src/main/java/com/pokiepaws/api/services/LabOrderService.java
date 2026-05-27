package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.laborder.CreateLabOrderRequest;
import com.pokiepaws.api.dto.laborder.LabOrderResponse;
import com.pokiepaws.api.dto.laborder.LabOrderStatusHistoryResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class LabOrderService {

  private static final Map<LabOrderStatus, Set<LabOrderStatus>> ALLOWED_TRANSITIONS =
      Map.of(
          LabOrderStatus.PENDING, Set.of(LabOrderStatus.CONFIRMED, LabOrderStatus.CANCELLED),
          LabOrderStatus.CONFIRMED, Set.of(LabOrderStatus.IN_PROGRESS, LabOrderStatus.CANCELLED),
          LabOrderStatus.IN_PROGRESS, Set.of(LabOrderStatus.COMPLETED, LabOrderStatus.CANCELLED),
          LabOrderStatus.COMPLETED, Set.of(),
          LabOrderStatus.CANCELLED, Set.of());

  private final LabOrderRepository labOrderRepository;
  private final LabOrderStatusHistoryRepository statusHistoryRepository;
  private final AnimalRepository animalRepository;
  private final VisitRepository visitRepository;
  private final VetRepository vetRepository;
  private final UserRepository userRepository;
  private final RealtimeNotificationService realtimeNotificationService;
  private final LabOrderWarehouseIntegrationService warehouseIntegrationService;

  @Transactional
  public LabOrderResponse createForAnimal(Long animalId, CreateLabOrderRequest request) {
    Animal animal =
        animalRepository
            .findById(animalId)
            .orElseThrow(() -> ApiException.notFound("Animal not found"));

    User currentUser = getCurrentUser();
    Vet vet =
        vetRepository
            .findByUserEmail(currentUser.getEmail())
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VET_NOT_FOUND));

    Clinic clinic = vet.getClinic();
    if (clinic == null) {
      throw ApiException.badRequest("Vet is not assigned to any clinic");
    }

    Visit visit = null;
    if (request.getVisitId() != null) {
      visit =
          visitRepository
              .findById(request.getVisitId())
              .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));
    }

    LabOrder labOrder =
        LabOrder.builder()
            .animal(animal)
            .visit(visit)
            .vet(vet)
            .clinic(clinic)
            .testType(request.getTestType())
            .clinicalReason(request.getClinicalReason())
            .priority(request.getPriority())
            .build();

    LabOrder saved = labOrderRepository.save(labOrder);

    statusHistoryRepository.save(
        LabOrderStatusHistory.builder()
            .labOrder(saved)
            .previousStatus(null)
            .newStatus(LabOrderStatus.PENDING)
            .changedByEmail(currentUser.getEmail())
            .build());

    scheduleWarehouseOrderCreation(saved.getId(), clinic.getId(), saved.getTestType());

    realtimeNotificationService.publishLabOrderCreated(saved);

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public LabOrderResponse getById(Long id) {
    return toResponse(
        labOrderRepository
            .findById(id)
            .orElseThrow(() -> ApiException.notFound("Lab order not found")));
  }

  @Transactional(readOnly = true)
  public List<LabOrderResponse> getByClinic(Long clinicId) {
    return labOrderRepository.findAllByClinicIdOrderByOrderedAtDesc(clinicId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<LabOrderResponse> getByAnimal(Long animalId) {
    return labOrderRepository.findAllByAnimalIdOrderByOrderedAtDesc(animalId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public LabOrderResponse updateStatus(Long id, LabOrderStatus newStatus) {
    LabOrder labOrder =
        labOrderRepository
            .findById(id)
            .orElseThrow(() -> ApiException.notFound("Lab order not found"));

    LabOrderStatus currentStatus = labOrder.getStatus();
    Set<LabOrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());

    if (!allowed.contains(newStatus)) {
      throw ApiException.badRequest(
          "Invalid status transition from "
              + currentStatus
              + " to "
              + newStatus
              + ". Allowed transitions: "
              + allowed);
    }

    String changedByEmail = getCurrentUser().getEmail();

    statusHistoryRepository.save(
        LabOrderStatusHistory.builder()
            .labOrder(labOrder)
            .previousStatus(currentStatus)
            .newStatus(newStatus)
            .changedByEmail(changedByEmail)
            .build());

    labOrder.setStatus(newStatus);
    if (newStatus == LabOrderStatus.COMPLETED || newStatus == LabOrderStatus.CANCELLED) {
      labOrder.setCompletedAt(LocalDateTime.now());
    }

    LabOrder saved = labOrderRepository.save(labOrder);

    realtimeNotificationService.publishLabOrderStatusUpdated(saved);

    return toResponse(saved);
  }

  private void scheduleWarehouseOrderCreation(Long labOrderId, Long clinicId, String testType) {
    if (!TransactionSynchronizationManager.isSynchronizationActive()) {
      warehouseIntegrationService.createWarehouseOrder(labOrderId, clinicId, testType);
      return;
    }
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            warehouseIntegrationService.createWarehouseOrder(labOrderId, clinicId, testType);
          }
        });
  }

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      throw ApiException.unauthorized(ApiErrorMessage.USER_NOT_FOUND);
    }
    return userRepository
        .findByEmail(auth.getName())
        .orElseThrow(() -> ApiException.unauthorized(ApiErrorMessage.USER_NOT_FOUND));
  }

  private LabOrderResponse toResponse(LabOrder o) {
    List<LabOrderStatusHistoryResponse> history =
        statusHistoryRepository.findAllByLabOrderIdOrderByChangedAtAsc(o.getId()).stream()
            .map(LabOrderService::toHistoryResponse)
            .toList();

    return LabOrderResponse.builder()
        .id(o.getId())
        .animalId(o.getAnimal().getId())
        .animalName(o.getAnimal().getName())
        .animalSpecies(o.getAnimal().getSpecies())
        .visitId(o.getVisit() != null ? o.getVisit().getId() : null)
        .vetUserId(o.getVet().getUserId())
        .vetFirstName(o.getVet().getFirstName())
        .vetLastName(o.getVet().getLastName())
        .clinicId(o.getClinic().getId())
        .testType(o.getTestType())
        .clinicalReason(o.getClinicalReason())
        .priority(o.getPriority())
        .status(o.getStatus())
        .warehouseOrderId(o.getWarehouseOrderId())
        .orderedAt(o.getOrderedAt())
        .completedAt(o.getCompletedAt())
        .statusHistory(history)
        .build();
  }

  private static LabOrderStatusHistoryResponse toHistoryResponse(LabOrderStatusHistory h) {
    return LabOrderStatusHistoryResponse.builder()
        .id(h.getId())
        .previousStatus(h.getPreviousStatus())
        .newStatus(h.getNewStatus())
        .changedByEmail(h.getChangedByEmail())
        .changedAt(h.getChangedAt())
        .build();
  }
}
