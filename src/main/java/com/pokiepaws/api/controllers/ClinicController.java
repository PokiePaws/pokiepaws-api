package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.clinic.ClinicRequest;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.services.ClinicService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {

  private final ClinicService clinicService;

  @GetMapping
  public List<Clinic> getAll() {
    return clinicService.getAll();
  }

  @GetMapping("/{id}")
  public Clinic getById(@PathVariable Long id) {
    return clinicService.getById(id);
  }

  @GetMapping("/city/{city}")
  public List<Clinic> getByCity(@PathVariable String city) {
    return clinicService.getByCity(city);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Clinic create(@Valid @RequestBody ClinicRequest request) {
    return clinicService.save(toClinic(request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Clinic update(@PathVariable Long id, @Valid @RequestBody ClinicRequest request) {
    Clinic clinic = toClinic(request);
    clinic.setId(id);
    return clinicService.save(clinic);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable Long id) {
    clinicService.delete(id);
  }

  private Clinic toClinic(ClinicRequest request) {
    return Clinic.builder()
        .clinicName(request.getClinicName())
        .regon(request.getRegon())
        .nip(request.getNip())
        .street(request.getStreet())
        .houseNumber(request.getHouseNumber())
        .apartmentNumber(request.getApartmentNumber())
        .postalCode(request.getPostalCode())
        .city(request.getCity())
        .country(request.getCountry())
        .workingHours(request.getWorkingHours())
        .phone(request.getPhone())
        .email(request.getEmail())
        .active(request.isActive())
        .build();
  }
}
