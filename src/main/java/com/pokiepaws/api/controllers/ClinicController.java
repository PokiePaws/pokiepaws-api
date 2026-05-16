package com.pokiepaws.api.controllers;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.services.ClinicService;
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
