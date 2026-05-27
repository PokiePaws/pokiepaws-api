package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.laborder.CreateLabOrderRequest;
import com.pokiepaws.api.dto.laborder.LabOrderResponse;
import com.pokiepaws.api.dto.laborder.UpdateLabOrderStatusRequest;
import com.pokiepaws.api.services.LabOrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LabOrderController {

  private final LabOrderService labOrderService;

  @PostMapping("/api/animals/{animalId}/lab-orders")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
  public LabOrderResponse create(
      @PathVariable Long animalId, @Valid @RequestBody CreateLabOrderRequest request) {
    return labOrderService.createForAnimal(animalId, request);
  }

  @GetMapping("/api/lab-orders/{id}")
  @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
  public LabOrderResponse getById(@PathVariable Long id) {
    return labOrderService.getById(id);
  }

  @GetMapping("/api/clinics/{clinicId}/lab-orders")
  @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
  public List<LabOrderResponse> getByClinic(@PathVariable Long clinicId) {
    return labOrderService.getByClinic(clinicId);
  }

  @GetMapping("/api/animals/{animalId}/lab-orders")
  @PreAuthorize("hasAnyRole('VET', 'ADMIN', 'OWNER')")
  public List<LabOrderResponse> getByAnimal(@PathVariable Long animalId) {
    return labOrderService.getByAnimal(animalId);
  }

  @PatchMapping("/api/lab-orders/{id}/status")
  @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
  public LabOrderResponse updateStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateLabOrderStatusRequest request) {
    return labOrderService.updateStatus(id, request.getStatus());
  }
}
