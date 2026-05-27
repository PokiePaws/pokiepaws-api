package com.pokiepaws.api.validators;

import com.pokiepaws.api.config.properties.VisitScheduleProperties;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Component;

@Component
public class VisitValidator {

  private final int slotMinutes;
  private final LocalTime workStart;
  private final LocalTime workEnd;
  private final VisitRepository visitRepository;

  public VisitValidator(
      VisitScheduleProperties visitScheduleProperties, VisitRepository visitRepository) {
    this.slotMinutes = visitScheduleProperties.getSlotMinutes();
    this.workStart = visitScheduleProperties.getWorkStart();
    this.workEnd = visitScheduleProperties.getWorkEnd();
    this.visitRepository = visitRepository;
  }

  public void validateVetBelongsToClinic(Vet vet, Clinic clinic) {
    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinic.getId())) {
      throw ApiException.badRequest(ApiErrorMessage.SELECTED_VET_DOES_NOT_BELONG_TO_CLINIC);
    }
  }

  public void validateRequestedSlot(Vet vet, LocalDateTime start) {
    if (start.getMinute() % slotMinutes != 0 || start.getSecond() != 0 || start.getNano() != 0) {
      throw ApiException.badRequest(ApiErrorMessage.SLOT_ALIGNMENT_INVALID);
    }

    LocalDateTime end = start.plusMinutes(slotMinutes);
    LocalDateTime dayStart = start.toLocalDate().atTime(workStart);
    LocalDateTime dayEnd = start.toLocalDate().atTime(workEnd);

    if (start.isBefore(dayStart) || end.isAfter(dayEnd)) {
      throw ApiException.badRequest(ApiErrorMessage.SELECTED_TIME_OUTSIDE_WORKING_HOURS);
    }

    if (!visitRepository.findOverlappingVisits(vet.getUserId(), start, end).isEmpty()) {
      throw ApiException.conflict(ApiErrorMessage.SELECTED_SLOT_ALREADY_TAKEN);
    }
  }

  public void validateDateRange(LocalDate from, LocalDate to) {
    if (from.isAfter(to)) {
      throw ApiException.badRequest(ApiErrorMessage.DATE_RANGE_INVALID);
    }
  }

  public void validateCurrentOwnerCanAccessVisit(Visit visit, Owner owner) {
    if (visit.getAnimal() == null
        || visit.getAnimal().getOwner() == null
        || !visit.getAnimal().getOwner().getUserId().equals(owner.getUserId())) {
      throw ApiException.forbidden(ApiErrorMessage.ACCESS_DENIED);
    }
  }

  public void validateCurrentOwnerCanCancelVisit(Visit visit, Owner owner) {
    if (visit.getAnimal() == null
        || visit.getAnimal().getOwner() == null
        || !visit.getAnimal().getOwner().getUserId().equals(owner.getUserId())) {
      throw ApiException.forbidden(ApiErrorMessage.CANCEL_FORBIDDEN);
    }
  }

  public void validateCurrentVetAssignedToVisit(Visit visit, Long vetUserId) {
    if (visit.getVet() == null || !visit.getVet().getUserId().equals(vetUserId)) {
      throw ApiException.forbidden(ApiErrorMessage.VET_NOT_ASSIGNED_TO_VISIT);
    }
  }

  public void validateVisitCanBeConfirmed(Visit visit) {
    if (visit.getStatus() == VisitStatus.CANCELLED) {
      throw ApiException.badRequest(ApiErrorMessage.CANCELLED_VISIT_CONFIRM_FORBIDDEN);
    }

    if (visit.getStatus() != VisitStatus.SCHEDULED) {
      throw ApiException.badRequest(ApiErrorMessage.ONLY_SCHEDULED_VISIT_CAN_BE_CONFIRMED);
    }
  }

  public void validateMedicalDataCanBeUpdated(Visit visit) {
    if (visit.getStatus() == VisitStatus.CANCELLED) {
      throw ApiException.badRequest(ApiErrorMessage.CANCELLED_VISIT_UPDATE_FORBIDDEN);
    }
  }

  public int slotMinutes() {
    return slotMinutes;
  }
}
