package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.prescription.CreatePrescriptionRequest;
import com.pokiepaws.api.dto.prescription.PrescriptionItemResponse;
import com.pokiepaws.api.dto.prescription.PrescriptionResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.WarehouseStockItemRepository;
import com.pokiepaws.api.validators.PrescriptionValidator;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

  private final Clock clock;
  private final VisitRepository visitRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final WarehouseStockItemRepository warehouseStockItemRepository;
  private final ClinicStockItemRepository clinicStockItemRepository;
  private final UserRepository userRepository;
  private final RealtimeNotificationService realtimeNotificationService;
  private final OwnerNotificationService ownerNotificationService;
  private final PrescriptionValidator prescriptionValidator;

  @Transactional
  public PrescriptionResponse createForVisit(Long visitId, CreatePrescriptionRequest request) {
    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));
    prescriptionValidator.validatePrescriptionCreationPreconditions(visit, visitId);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    Long currentUserId =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> ApiException.unauthorized(ApiErrorMessage.USER_NOT_FOUND))
            .getId();

    prescriptionValidator.validateCurrentVetCanCreatePrescription(visit, currentUserId);
    Clinic clinic = visit.getClinic();

    Prescription prescription =
        Prescription.builder()
            .visit(visit)
            .vet(visit.getVet())
            .clinic(clinic)
            .recommendationDate(request.getRecommendationDate())
            .creationDate(LocalDate.now(clock))
            .build();

    request
        .getItems()
        .forEach(
            i -> {
              WarehouseStockItem stockItem =
                  warehouseStockItemRepository
                      .findById(i.getProductId())
                      .orElseThrow(
                          () ->
                              ApiException.notFound(
                                  ApiErrorMessage.PRODUCT_NOT_FOUND + i.getProductId()));

              ClinicStockItem clinicStock =
                  clinicStockItemRepository
                      .findByClinicIdAndStockItemId(clinic.getId(), stockItem.getId())
                      .orElseThrow(
                          () ->
                              ApiException.badRequest(
                                  ApiErrorMessage.PRODUCT_NOT_AVAILABLE_IN_CLINIC_STOCK
                                      + stockItem.getId()));

              prescriptionValidator.validateStockAvailable(clinicStock, i.getQuantityPackages());
              clinicStock.setQuantityPackages(
                  clinicStock.getQuantityPackages() - i.getQuantityPackages());
              clinicStockItemRepository.save(clinicStock);
              realtimeNotificationService.publishClinicStockUpdated(clinicStock);

              PrescriptionItem item =
                  PrescriptionItem.builder()
                      .stockItem(stockItem)
                      .quantityPackages(i.getQuantityPackages())
                      .dosage(i.getDosage())
                      .treatmentTime(i.getTreatmentTime())
                      .build();

              prescription.addItem(item);
            });

    Prescription saved = prescriptionRepository.save(prescription);
    realtimeNotificationService.publishPrescriptionCreated(saved);
    ownerNotificationService.prescriptionCreated(saved);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public PrescriptionResponse getForVisit(Long visitId) {
    Prescription prescription =
        prescriptionRepository
            .findByVisitId(visitId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.PRESCRIPTION_NOT_FOUND));
    return toResponse(prescription);
  }

  @Transactional(readOnly = true)
  public PrescriptionResponse getForVisitForCurrentOwner(Long visitId) {
    Visit visit = getVisit(visitId);
    User currentUser = getCurrentUser();
    prescriptionValidator.validateCurrentOwnerCanAccessPrescription(visit, currentUser);

    return getForVisit(visitId);
  }

  @Transactional(readOnly = true)
  public PrescriptionResponse getForVisitForCurrentUser(Long visitId) {
    Visit visit = getVisit(visitId);
    User currentUser = getCurrentUser();

    if (currentUser.getRole() == Role.OWNER) {
      prescriptionValidator.validateCurrentOwnerCanAccessPrescription(visit, currentUser);
    } else {
      prescriptionValidator.validateCurrentVetOrAdminCanAccessPrescription(visit, currentUser);
    }

    return getForVisit(visitId);
  }

  @Transactional(readOnly = true)
  public PrescriptionResponse getForVisitForCurrentVetOrAdmin(Long visitId) {
    Visit visit = getVisit(visitId);
    User currentUser = getCurrentUser();
    prescriptionValidator.validateCurrentVetOrAdminCanAccessPrescription(visit, currentUser);

    return getForVisit(visitId);
  }

  private Visit getVisit(Long visitId) {
    return visitRepository
        .findById(visitId)
        .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VISIT_NOT_FOUND));
  }

  private User getCurrentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> ApiException.unauthorized(ApiErrorMessage.USER_NOT_FOUND));
  }

  private static PrescriptionResponse toResponse(Prescription prescription) {
    return PrescriptionResponse.builder()
        .id(prescription.getId())
        .visitId(prescription.getVisit().getId())
        .vetUserId(prescription.getVet().getUserId())
        .clinicId(prescription.getClinic().getId())
        .recommendationDate(prescription.getRecommendationDate())
        .creationDate(prescription.getCreationDate())
        .items(
            prescription.getItems().stream()
                .map(
                    item ->
                        PrescriptionItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getStockItem().getId())
                            .productName(item.getStockItem().getName())
                            .quantityPackages(item.getQuantityPackages())
                            .dosage(item.getDosage())
                            .treatmentTime(item.getTreatmentTime())
                            .build())
                .toList())
        .build();
  }
}
