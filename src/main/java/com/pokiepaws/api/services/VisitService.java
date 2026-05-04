package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.visit.CreateVisitRequest;
import com.pokiepaws.api.dto.visit.UpdateVisitMedicalDataRequest;
import com.pokiepaws.api.dto.visit.VisitResponse;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class VisitService {

  private static final int SLOT_MINUTES = 30;
  private static final LocalTime WORK_START = LocalTime.of(9, 0);
  private static final LocalTime WORK_END = LocalTime.of(17, 0);

  private final VisitRepository visitRepository;
  private final AnimalRepository animalRepository;
  private final ClinicRepository clinicRepository;
  private final VetRepository vetRepository;
  private final OwnerRepository ownerRepository;
  private final UserRepository userRepository;
  private static final String VISIT_NOT_FOUND_MESSAGE = "Visit not found";

  private Long getCurrentUserIdOrThrow() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
        .getId();
  }

  private String getCurrentEmailOrThrow() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }

  private Owner getOwnerByEmailOrThrow(String email) {
    return ownerRepository
        .findByUserEmail(email)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner profile not found"));
  }

  @Transactional
  public VisitResponse create(CreateVisitRequest req) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    Animal animal =
        animalRepository
            .findByIdAndOwnerAndActiveTrue(req.getAnimalId(), owner)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "You are not the owner of this animal"));

    Clinic clinic =
        clinicRepository
            .findById(req.getClinicId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

    Vet vet =
        vetRepository
            .findById(req.getVetUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vet not found"));

    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinic.getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Selected vet does not belong to selected clinic");
    }

    LocalDateTime start = req.getStartsAt();

    if (start.getMinute() % SLOT_MINUTES != 0 || start.getSecond() != 0 || start.getNano() != 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Start time must align to 30-minute slots");
    }

    LocalDateTime end = start.plusMinutes(SLOT_MINUTES);
    LocalDateTime dayStart = start.toLocalDate().atTime(WORK_START);
    LocalDateTime dayEnd = start.toLocalDate().atTime(WORK_END);

    if (start.isBefore(dayStart) || end.isAfter(dayEnd)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Selected time is outside working hours");
    }

    if (!visitRepository.findOverlappingVisits(vet.getUserId(), start, end).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot is already taken");
    }

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

    return toResponse(visitRepository.save(visit));
  }

  @Transactional(readOnly = true)
  public VisitResponse getByIdForCurrentOwner(Long visitId) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, VISIT_NOT_FOUND_MESSAGE));

    if (!visit.getAnimal().getOwner().getUserId().equals(owner.getUserId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    return toResponse(visit);
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getByAnimalForCurrentOwner(Long animalId) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    animalRepository
        .findByIdAndOwnerAndActiveTrue(animalId, owner)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not the owner of this animal"));

    return visitRepository.findAllByAnimalId(animalId).stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyUpcomingVisits() {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    return visitRepository
        .findAllByAnimalOwnerUserIdAndStartsAtAfterOrderByStartsAtAsc(
            owner.getUserId(), LocalDateTime.now())
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyVisitsInRange(LocalDate from, LocalDate to) {
    if (from.isAfter(to)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be <= 'to'");
    }

    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    return visitRepository
        .findAllByAnimalOwnerUserIdAndStartsAtBetween(
            owner.getUserId(), from.atStartOfDay(), to.atTime(LocalTime.MAX))
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional
  public VisitResponse cancelForCurrentOwner(Long visitId) {
    Owner owner = getOwnerByEmailOrThrow(getCurrentEmailOrThrow());

    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, VISIT_NOT_FOUND_MESSAGE));

    if (visit.getAnimal() == null
        || visit.getAnimal().getOwner() == null
        || !visit.getAnimal().getOwner().getUserId().equals(owner.getUserId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot cancel this visit");
    }

    if (visit.getStatus() != VisitStatus.CANCELLED) {
      visit.setStatus(VisitStatus.CANCELLED);
      visitRepository.save(visit);
    }

    return toResponse(visit);
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyUpcomingVisitsForCurrentVet() {
    Long vetUserId = getCurrentUserIdOrThrow();

    return visitRepository
        .findAllByVetUserIdAndStatusNotAndStartsAtAfterOrderByStartsAtAsc(
            vetUserId, VisitStatus.CANCELLED, LocalDateTime.now())
        .stream()
        .map(VisitService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VisitResponse> getMyVisitsInRangeForCurrentVet(LocalDate from, LocalDate to) {
    if (from.isAfter(to)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be <= 'to'");
    }

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
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, VISIT_NOT_FOUND_MESSAGE));

    if (visit.getVet() == null || !visit.getVet().getUserId().equals(vetUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the vet for this visit");
    }

    if (visit.getStatus() == VisitStatus.CANCELLED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update cancelled visit");
    }

    visit.setDisease(req.getDisease());
    visit.setDiagnosis(req.getDiagnosis());
    visit.setRecommendations(req.getRecommendations());

    return toResponse(visitRepository.save(visit));
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
