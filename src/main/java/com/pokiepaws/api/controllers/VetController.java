package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.vet.VetRequest;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.services.VetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
  public List<Vet> getByClinic(@PathVariable Long clinicId) {
    return vetService.getByClinic(clinicId);
  }

  @PostMapping
  public Vet create(@RequestBody VetRequest request) {
    return vetService.save(request);
  }

  @PutMapping("/{id}")
  public Vet update(@PathVariable Long id, @RequestBody VetRequest request) {
    return vetService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    vetService.delete(id);
  }
}
