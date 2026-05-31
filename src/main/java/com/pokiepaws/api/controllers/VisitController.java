package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.visit.CreateVisitRequest;
import com.pokiepaws.api.dto.visit.VisitResponse;
import com.pokiepaws.api.services.VisitService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class VisitController {

  private final VisitService visitService;

  @GetMapping("/animals/{animalId}/visits")
  @PreAuthorize("hasRole('OWNER')")
  public List<VisitResponse> getByAnimal(@PathVariable Long animalId) {
    return visitService.getByAnimalForCurrentOwner(animalId);
  }

  @PostMapping("/visits")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('OWNER', 'VET')")
  public VisitResponse create(@Valid @RequestBody CreateVisitRequest req) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isVet = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_VET"));
    return isVet
        ? visitService.createForVet(req)
        : visitService.create(req);
  }

  @PatchMapping("/visits/{id}/cancel")
  @PreAuthorize("hasAnyRole('OWNER', 'VET')")
  public VisitResponse cancel(@PathVariable Long id) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean isVet = auth.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_VET"));
    return isVet
        ? visitService.cancelForCurrentVet(id)
        : visitService.cancelForCurrentOwner(id);
  }

  @GetMapping("/owners/me/visits/upcoming")
  @PreAuthorize("hasRole('OWNER')")
  public List<VisitResponse> getMyUpcomingVisits() {
    return visitService.getMyUpcomingVisits();
  }

  @GetMapping("/owners/me/visits")
  @PreAuthorize("hasRole('OWNER')")
  public List<VisitResponse> getMyVisitsInRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return visitService.getMyVisitsInRange(from, to);
  }

  @GetMapping("/visits/{id}")
  @PreAuthorize("hasRole('OWNER')")
  public VisitResponse getById(@PathVariable Long id) {
    return visitService.getByIdForCurrentOwner(id);
  }
}
