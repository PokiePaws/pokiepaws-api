package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.ClinicAssortmentItem;
import com.pokiepaws.api.repositories.ClinicAssortmentItemRepository;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.LabOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles creation of a warehouse/assortment order that corresponds to a lab order. Runs in a
 * dedicated transaction (REQUIRES_NEW) so that warehouse integration failures never roll back the
 * primary lab order transaction.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LabOrderWarehouseIntegrationService {

  private final ClinicAssortmentItemRepository clinicAssortmentItemRepository;
  private final ClinicRepository clinicRepository;
  private final LabOrderRepository labOrderRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createWarehouseOrder(Long labOrderId, Long clinicId, String testType) {
    try {
      Clinic clinic =
          clinicRepository
              .findById(clinicId)
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Clinic not found during warehouse integration: " + clinicId));

      ClinicAssortmentItem warehouseOrder =
          ClinicAssortmentItem.builder()
              .clinic(clinic)
              .name(testType)
              .amount(1)
              .description("Automatyczne zamówienie do zlecenia lab. #" + labOrderId)
              .category("LAB")
              .build();

      ClinicAssortmentItem saved = clinicAssortmentItemRepository.save(warehouseOrder);
      log.info(
          "Warehouse order {} created for lab order {}. testType='{}'",
          saved.getId(),
          labOrderId,
          testType);

      labOrderRepository
          .findById(labOrderId)
          .ifPresent(
              labOrder -> {
                labOrder.setWarehouseOrderId(saved.getId());
                labOrderRepository.save(labOrder);
              });

    } catch (Exception e) {
      log.error(
          "Warehouse integration failed for lab order {} (clinicId={}, testType='{}'): {}",
          labOrderId,
          clinicId,
          testType,
          e.getMessage(),
          e);
    }
  }
}
