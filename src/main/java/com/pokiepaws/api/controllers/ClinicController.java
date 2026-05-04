package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.clinic.ClinicResponse;
import com.pokiepaws.api.dto.vet.VetResponse;
import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.services.ClinicService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {

  private final ClinicService clinicService;

  // endpointy dla właściciela zwierzęcia (OWNER) — zwracają DTO
  @GetMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  public List<ClinicResponse> getAll() {
    return clinicService.getAllAsDto();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  public ClinicResponse getById(@PathVariable Long id) {
    return clinicService.getByIdAsDto(id);
  }

  @GetMapping("/city/{city}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  public List<ClinicResponse> getByCity(@PathVariable String city) {
    return clinicService.getByCityAsDto(city);
  }

  // lista weterynarzy gabinetu — potrzebna przy umawianiu wizyty (PP-11)
  @GetMapping("/{id}/vets")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  public List<VetResponse> getVets(@PathVariable Long id) {
    return clinicService.getVetsByClinicId(id);
  }

  // dostępne sloty — potrzebne przy umawianiu wizyty (PP-11)
  @GetMapping("/{id}/vets/{vetUserId}/slots")
  @PreAuthorize("hasRole('OWNER')")
  public AvailableSlotsResponse getSlots(
      @PathVariable Long id,
      @PathVariable Long vetUserId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return clinicService.getAvailableSlots(id, vetUserId, date);
  }

  // endpointy admina — zarządzanie klinikami
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Clinic create(@RequestBody Clinic clinic) {
    return clinicService.save(clinic);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Clinic update(@PathVariable Long id, @RequestBody Clinic clinic) {
    clinic.setId(id);
    return clinicService.save(clinic);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable Long id) {
    clinicService.delete(id);
  }
}
