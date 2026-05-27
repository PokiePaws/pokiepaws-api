package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.vet.VetWorkingHoursRequest;
import com.pokiepaws.api.dto.vet.VetWorkingHoursResponse;
import com.pokiepaws.api.services.VetWorkingHoursService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vets/me/working-hours")
@PreAuthorize("hasRole('VET')")
public class VetWorkingHoursController {

  private final VetWorkingHoursService workingHoursService;

  @GetMapping
  public List<VetWorkingHoursResponse> get(Authentication authentication) {
    return workingHoursService.getCurrentVetWorkingHours(authentication.getName());
  }

  @PutMapping
  public List<VetWorkingHoursResponse> replace(
      Authentication authentication, @Valid @RequestBody List<VetWorkingHoursRequest> requests) {
    return workingHoursService.replaceCurrentVetWorkingHours(authentication.getName(), requests);
  }
}
