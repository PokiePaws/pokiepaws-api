package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.visit.CreateVisitRequest;
import com.pokiepaws.api.dto.visit.UpdateVisitMedicalDataRequest;
import com.pokiepaws.api.dto.visit.VisitResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import com.pokiepaws.api.validators.VisitValidator;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitService {

  private final VisitRepository visitRepository;
  private final AnimalRepository animalRepository;
  private final ClinicRepository clinicRepository;
  private final VetRepository vetRepository;
  private final OwnerRepository ownerRepository;
  private final UserRepository userRepository;
  private final RealtimeNotificationService realtimeNotificationService;
  private final OwnerNotificationService ownerNotificationService;
  private final VisitValidator visitValidator;
  private final Clock clock;

  private Long getCurrentUserIdOrThrow() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> ApiException.unauthorized(ApiErrorMessage.USER_NOT_FOUND))
        .getId();
  }

  private String getCurrentEmailOrThrow() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  private Owner getOwnerByEmailOrThrow(String email) {
    return ownerRepository
        .findByUserEmail(email)
        .orElseThrow(() -> ApiException.forbidden(ApiErrorMessage.OWNER_PROFILE_NOT_FOUND));
  }

  @Transactional
  public VisitResponse create(CreateVisitRequest req) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    Animal animal =
        animalRepository
            .findByIdAndOwnerAndActiveTrue(req.getAnimalId(), owner)
            .orElseThrow(() -> ApiException.forbidden(ApiErrorMessage.ANIMAL_NOT_OWNED));

    Clinic clinic =
        clinicRepository
            .findById(req.getClinicId())
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.CLINIC_NOT_FOUND));

    Vet vet =
        vetRepository
            .findById(req.getVetUserId())
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VET_NOT_FOUND));

    visitValidator.validateVetBelongsToClinic(vet, clinic);
    visitValidator.validateRequestedSlot(vet, req.getStartsAt());

    LocalDateTime start = req.getStartsAt();
    LocalDateTime end = start.plusMinutes(visitValidator.slotMinutes());

    Visit visit =
        Visit.builder()
            .animal(animal)
            .clinic(clinic)
            .vet(vet)
            .startsAt(start)
            .endsAt(end)
            .description(req.getDescription())
            .status(VisitStatus.SCHEDULED)
            .used(false)
            .build();

    Visit saved = visitRepository.save(visit);

    realtimeNotificationService.publishVisitCreated(saved);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public VisitResponse getByIdForCurrentOwner(Long visitId) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));

    visitValidator.validateCurrentOwnerCanAccessVisit(visit, owner);

    return toResponse(visit);
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getByAnimalForCurrentOwner(Long animalId) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    animalRepository
        .findByIdAndOwnerAndActiveTrue(animalId, owner)
        .orElseThrow(() -> ApiException.forbidden(ApiErrorMessage.ANIMAL_NOT_OWNED));

    return visitRepository.findAllByAnimalId(animalId).stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyUpcomingVisits() {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    return visitRepository
        .findAllByAnimalOwnerUserIdAndStartsAtAfterOrderByStartsAtAsc(
            owner.getUserId(), LocalDateTime.now(clock))
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyVisitsInRange(LocalDate from, LocalDate to) {
    visitValidator.validateDateRange(from, to);

    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    return visitRepository
        .findAllByAnimalOwnerUserIdAndStartsAtBetween(
            owner.getUserId(), from.atStartOfDay(), to.atTime(LocalTime.MAX))
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional
  public VisitResponse confirmForCurrentVet(Long visitId) {
    Long vetUserId = getCurrentUserIdOrThrow();

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));

    visitValidator.validateCurrentVetAssignedToVisit(visit, vetUserId);
    visitValidator.validateVisitCanBeConfirmed(visit);

    visit.setStatus(VisitStatus.CONFIRMED);
    Visit saved = visitRepository.save(visit);

    ownerNotificationService.visitConfirmed(saved);

    return toResponse(saved);
  }

  @Transactional
  public VisitResponse cancelForCurrentOwner(Long visitId) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));

    visitValidator.validateCurrentOwnerCanCancelVisit(visit, owner);

    if (visit.getStatus() != VisitStatus.CANCELLED) {
      visit.setStatus(VisitStatus.CANCELLED);
      visitRepository.save(visit);

      realtimeNotificationService.publishVisitCancelled(visit);
      ownerNotificationService.visitCancelled(visit);
    }

    return toResponse(visit);
  }

  @Transactional
  public VisitResponse cancelForCurrentVet(Long visitId) {
    Long vetUserId = getCurrentUserIdOrThrow();

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));

    visitValidator.validateCurrentVetAssignedToVisit(visit, vetUserId);
    visitValidator.validateVisitCanBeCancelledByVet(visit);

    visit.setStatus(VisitStatus.CANCELLED);
    Visit saved = visitRepository.save(visit);

    realtimeNotificationService.publishVisitCancelled(saved);
    ownerNotificationService.visitCancelledByVet(saved);

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyUpcomingVisitsForCurrentVet() {
    Long vetUserId = getCurrentUserIdOrThrow();

    return visitRepository
        .findAllByVetUserIdAndStatusNotAndStartsAtAfterOrderByStartsAtAsc(
            vetUserId, VisitStatus.CANCELLED, LocalDateTime.now(clock))
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyVisitsInRangeForCurrentVet(LocalDate from, LocalDate to) {
    visitValidator.validateDateRange(from, to);

    Long vetUserId = getCurrentUserIdOrThrow();

    return visitRepository
        .findAllByVetUserIdAndStatusNotAndStartsAtBetweenOrderByStartsAtAsc(
            vetUserId, VisitStatus.CANCELLED, from.atStartOfDay(), to.atTime(LocalTime.MAX))
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional
  public VisitResponse updateMedicalData(Long visitId, UpdateVisitMedicalDataRequest req) {
    Long vetUserId = getCurrentUserIdOrThrow();

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));

    visitValidator.validateCurrentVetAssignedToVisit(visit, vetUserId);
    visitValidator.validateMedicalDataCanBeUpdated(visit);

    visit.setDisease(req.getDisease());
    visit.setDiagnosis(req.getDiagnosis());
    visit.setRecommendations(req.getRecommendations());

    Visit saved = visitRepository.save(visit);
    realtimeNotificationService.publishVisitMedicalDataUpdated(saved);
    ownerNotificationService.visitMedicalDataUpdated(saved);

    return toResponse(saved);
  }

  static VisitResponse toResponse(Visit v) {
    return VisitResponse.builder()
        .id(v.getId())
        .animalId(v.getAnimal().getId())
        .clinicId(v.getClinic().getId())
        .vetUserId(v.getVet() != null ? v.getVet().getUserId() : null)
        .startsAt(v.getStartsAt())
        .endsAt(v.getEndsAt())
        .description(v.getDescription())
        .disease(v.getDisease())
        .diagnosis(v.getDiagnosis())
        .recommendations(v.getRecommendations())
        .status(v.getStatus())
        .used(v.isUsed())
        .build();
  }
}
