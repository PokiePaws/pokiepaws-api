package com.pokiepaws.api.validators;

import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrescriptionValidator {

  private final PrescriptionRepository prescriptionRepository;

  public void validatePrescriptionCreationPreconditions(Visit visit, Long visitId) {
    if (prescriptionRepository.existsByVisitId(visitId)) {
      throw ApiException.conflict(ApiErrorMessage.PRESCRIPTION_ALREADY_EXISTS);
    }

    if (visit.getVet() == null) {
      throw ApiException.badRequest(ApiErrorMessage.VISIT_HAS_NO_ASSIGNED_VET);
    }
  }

  public void validateCurrentVetCanCreatePrescription(Visit visit, Long currentUserId) {
    if (!currentUserId.equals(visit.getVet().getUserId())) {
      throw ApiException.forbidden(ApiErrorMessage.VISIT_NOT_ASSIGNED_TO_CURRENT_VET);
    }
  }

  public void validateStockAvailable(ClinicStockItem stock, int requestedQuantity) {
    if (stock.getQuantityPackages() < requestedQuantity) {
      throw ApiException.badRequest(
          "Not enough stock for stockItemId="
              + stock.getStockItem().getId()
              + " (available="
              + stock.getQuantityPackages()
              + ", requested="
              + requestedQuantity
              + ")");
    }
  }

  public void validateCurrentOwnerCanAccessPrescription(Visit visit, User currentUser) {
    if (visit.getAnimal() == null
        || visit.getAnimal().getOwner() == null
        || !visit.getAnimal().getOwner().getUserId().equals(currentUser.getId())) {
      throw ApiException.forbidden(ApiErrorMessage.VISIT_NOT_OWNED);
    }
  }

  public void validateCurrentVetOrAdminCanAccessPrescription(Visit visit, User currentUser) {
    if (currentUser.getRole() != Role.ADMIN
        && (visit.getVet() == null || !visit.getVet().getUserId().equals(currentUser.getId()))) {
      throw ApiException.forbidden(ApiErrorMessage.ACCESS_DENIED);
    }
  }
}
