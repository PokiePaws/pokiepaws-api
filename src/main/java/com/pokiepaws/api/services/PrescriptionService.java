package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.CreatePrescriptionRequest;
import com.pokiepaws.api.dto.PrescriptionItemResponse;
import com.pokiepaws.api.dto.PrescriptionResponse;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import com.pokiepaws.api.repositories.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

  private final Clock clock;
  private final VisitRepository visitRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final ProductRepository productRepository;
  private final ClinicStockItemRepository clinicStockItemRepository;
  private final UserRepository userRepository;

  @Transactional
  public PrescriptionResponse createForVisit(Long visitId, CreatePrescriptionRequest request) {
    Visit visit =
        visitRepository
            .findById(visitId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

    if (prescriptionRepository.existsByVisitId(visitId)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Prescription already exists for this visit");
    }
    if (visit.getVet() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit has no assigned vet");
    }
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    Long currentUserId =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();

    if (!currentUserId.equals(visit.getVet().getUserId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "You are not assigned vet for this visit");
    }
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
              Product product =
                  productRepository
                      .findById(i.getProductId())
                      .orElseThrow(
                          () ->
                              new ResponseStatusException(
                                  HttpStatus.NOT_FOUND, "Product not found: " + i.getProductId()));

              ClinicStockItem stock =
                  clinicStockItemRepository
                      .findByClinicIdAndProductId(clinic.getId(), product.getId())
                      .orElseThrow(
                          () ->
                              new ResponseStatusException(
                                  HttpStatus.BAD_REQUEST,
                                  "Product not available in clinic stock: " + product.getId()));

              if (stock.getQuantityPackages() < i.getQuantityPackages()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Not enough stock for productId="
                        + product.getId()
                        + " (available="
                        + stock.getQuantityPackages()
                        + ", requested="
                        + i.getQuantityPackages()
                        + ")");
              }

              stock.setQuantityPackages(stock.getQuantityPackages() - i.getQuantityPackages());
              clinicStockItemRepository.save(stock);

              PrescriptionItem item =
                  PrescriptionItem.builder()
                      .product(product)
                      .quantityPackages(i.getQuantityPackages())
                      .dosage(i.getDosage())
                      .treatmentTime(i.getTreatmentTime())
                      .build();

              prescription.addItem(item);
            });

    Prescription saved = prescriptionRepository.save(prescription);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public PrescriptionResponse getForVisit(Long visitId) {
    Prescription prescription =
        prescriptionRepository
            .findByVisitId(visitId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Prescription not found for this visit"));
    return toResponse(prescription);
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
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantityPackages(item.getQuantityPackages())
                            .dosage(item.getDosage())
                            .treatmentTime(item.getTreatmentTime())
                            .build())
                .toList())
        .build();
  }
}
