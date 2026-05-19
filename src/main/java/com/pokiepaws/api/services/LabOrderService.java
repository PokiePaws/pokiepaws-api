package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.laborder.CreateLabOrderRequest;
import com.pokiepaws.api.dto.laborder.LabOrderResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabOrderService {

  private final LabOrderRepository labOrderRepository;
  private final AnimalRepository animalRepository;
  private final VisitRepository visitRepository;
  private final VetRepository vetRepository;
  private final UserRepository userRepository;

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

    return toResponse(labOrderRepository.save(labOrder));
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
        .map(LabOrderService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<LabOrderResponse> getByVet(Long vetUserId) {
    return labOrderRepository.findAllByVetUserIdOrderByOrderedAtDesc(vetUserId).stream()
        .map(LabOrderService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<LabOrderResponse> getByAnimal(Long animalId) {
    return labOrderRepository.findAllByAnimalIdOrderByOrderedAtDesc(animalId).stream()
        .map(LabOrderService::toResponse)
        .toList();
  }

  @Transactional
  public LabOrderResponse updateStatus(Long id, LabOrderStatus newStatus) {
    LabOrder labOrder =
        labOrderRepository
            .findById(id)
            .orElseThrow(() -> ApiException.notFound("Lab order not found"));

    labOrder.setStatus(newStatus);
    if (newStatus == LabOrderStatus.COMPLETED || newStatus == LabOrderStatus.CANCELLED) {
      labOrder.setCompletedAt(LocalDateTime.now());
    }

    return toResponse(labOrderRepository.save(labOrder));
  }

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return userRepository
        .findByEmail(auth.getName())
        .orElseThrow(() -> ApiException.unauthorized(ApiErrorMessage.USER_NOT_FOUND));
  }

  private static LabOrderResponse toResponse(LabOrder o) {
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
        .orderedAt(o.getOrderedAt())
        .completedAt(o.getCompletedAt())
        .build();
  }
}
