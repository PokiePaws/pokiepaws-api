package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.vet.VetListResponse;
import com.pokiepaws.api.dto.vet.VetRequest;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.services.VetService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vets")
@RequiredArgsConstructor
public class VetController {

  private final VetService vetService;

  @GetMapping
  public List<Vet> getAll() {
    return vetService.getAll();
  }

  @GetMapping("/{id}")
  public Vet getById(@PathVariable Long id) {
    return vetService.getById(id);
  }

  @GetMapping("/clinic/{clinicId}")
  public List<VetListResponse> getByClinic(@PathVariable Long clinicId) {
    return vetService.getListItemsByClinic(clinicId);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public Vet create(@Valid @RequestBody VetRequest request) {
    return vetService.save(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public Vet update(@PathVariable Long id, @Valid @RequestBody VetRequest request) {
    return vetService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public void delete(@PathVariable Long id) {
    vetService.delete(id);
  }
}
