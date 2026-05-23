package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.animal.AnimalResponse;
import com.pokiepaws.api.dto.vet.CreateVetVisitRequest;
import com.pokiepaws.api.dto.vet.RegisterPatientRequest;
import com.pokiepaws.api.dto.visit.UpdateVisitMedicalDataRequest;
import com.pokiepaws.api.dto.visit.VisitResponse;
import com.pokiepaws.api.services.VetService;
import com.pokiepaws.api.services.VisitService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vets/me/visits")
@PreAuthorize("hasRole('VET')")
public class VetVisitsController {

  private final VisitService visitService;
  private final VetService vetService;

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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public VisitResponse createVisit(
      Authentication authentication, @Valid @RequestBody CreateVetVisitRequest req) {
    return vetService.createVisit(authentication.getName(), req);
  }

  @PatchMapping("/{id}/medical-data")
  public VisitResponse updateMedicalData(
      @PathVariable Long id, @RequestBody UpdateVisitMedicalDataRequest req) {
    return visitService.updateMedicalData(id, req);
  }

  @PostMapping("/{id}/confirm")
  public VisitResponse confirm(@PathVariable Long id) {
    return visitService.confirmForCurrentVet(id);
  }

  @PatchMapping("/{id}/cancel")
  public VisitResponse cancel(@PathVariable Long id) {
    return visitService.cancelForCurrentVet(id);
  }

  @PostMapping("/patients")
  @ResponseStatus(HttpStatus.CREATED)
  public AnimalResponse registerPatient(@Valid @RequestBody RegisterPatientRequest req) {
    return vetService.registerPatient(req);
  }
}
