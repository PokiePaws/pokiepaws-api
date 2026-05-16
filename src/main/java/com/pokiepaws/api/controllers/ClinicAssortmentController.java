package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.warehouse.ClinicAssortmentItemRequest;
import com.pokiepaws.api.dto.warehouse.ClinicAssortmentItemResponse;
import com.pokiepaws.api.dto.warehouse.UpdateStatusRequest;
import com.pokiepaws.api.services.ClinicAssortmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class ClinicAssortmentController {

  private final ClinicAssortmentService service;

  @GetMapping
  @PreAuthorize("hasRole('WAREHOUSE')")
  public List<ClinicAssortmentItemResponse> getAll(
      @RequestParam(required = false) Long clinicId,
      @RequestParam(required = false) String status) {
    return service.getAll(clinicId, status);
  }

  @PostMapping
  @PreAuthorize("hasRole('VET')")
  public ClinicAssortmentItemResponse create(
      @Valid @RequestBody ClinicAssortmentItemRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('WAREHOUSE')")
  public ClinicAssortmentItemResponse updateStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
    return service.updateStatus(id, request.getStatus());
  }
}
