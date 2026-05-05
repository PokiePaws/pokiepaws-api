package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.prescription.CreatePrescriptionRequest;
import com.pokiepaws.api.dto.prescription.PrescriptionResponse;
import com.pokiepaws.api.services.PrescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/visits")
public class PrescriptionController {

  private final PrescriptionService prescriptionService;

  @PostMapping("/{id}/prescription")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('VET','ADMIN')")
  public PrescriptionResponse create(
      @PathVariable("id") Long visitId, @Valid @RequestBody CreatePrescriptionRequest request) {
    return prescriptionService.createForVisit(visitId, request);
  }

  @GetMapping("/{id}/prescription")
  @PreAuthorize("hasAnyRole('VET','ADMIN')")
  public PrescriptionResponse get(@PathVariable("id") Long visitId) {
    return prescriptionService.getForVisit(visitId);
  }
}
