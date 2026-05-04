package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.services.AvailabilityService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AvailabilityController {

  private final AvailabilityService availabilityService;

  @GetMapping("/clinics/{clinicId}/vets/{vetUserId}/available-slots")
  @PreAuthorize("hasRole('OWNER')")
  public AvailableSlotsResponse getAvailableSlots(
      @PathVariable Long clinicId,
      @PathVariable Long vetUserId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return availabilityService.getAvailableSlots(clinicId, vetUserId, date);
  }
}
