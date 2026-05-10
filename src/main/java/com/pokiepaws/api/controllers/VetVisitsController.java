package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.visit.UpdateVisitMedicalDataRequest;
import com.pokiepaws.api.dto.visit.VisitResponse;
import com.pokiepaws.api.services.VisitService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vets/me/visits")
@PreAuthorize("hasRole('VET')")
public class VetVisitsController {

  private final VisitService visitService;

  @GetMapping("/upcoming")
  public List<VisitResponse> upcoming() {
    return visitService.getMyUpcomingVisitsForCurrentVet();
  }

  @GetMapping
  public List<VisitResponse> range(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return visitService.getMyVisitsInRangeForCurrentVet(from, to);
  }

  @PatchMapping("/{id}/medical-data")
  public VisitResponse updateMedicalData(
      @PathVariable Long id, @RequestBody UpdateVisitMedicalDataRequest req) {
    return visitService.updateMedicalData(id, req);
  }
}
