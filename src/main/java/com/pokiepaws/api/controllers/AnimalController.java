package com.pokiepaws.api.controllers;

import com.pokiepaws.api.config.OpenApiConfig;
import com.pokiepaws.api.dto.animal.AnimalRequest;
import com.pokiepaws.api.dto.animal.AnimalResponse;
import com.pokiepaws.api.services.AnimalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RequestMapping(value = "/api/animals", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AnimalController {

  private final AnimalService animalService;

  @GetMapping
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<List<AnimalResponse>> getMyAnimals() {
    return ResponseEntity.ok(animalService.getMyAnimals());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<AnimalResponse> getAnimal(@PathVariable Long id) {
    return ResponseEntity.ok(animalService.getAnimal(id));
  }

  @GetMapping("/owner/{ownerId}")
  @PreAuthorize("hasRole('VET')")
  public ResponseEntity<List<AnimalResponse>> getAnimalsForVet(@PathVariable Long ownerId) {
    return ResponseEntity.ok(animalService.getAnimalsByOwner(ownerId));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<AnimalResponse> addAnimal(@Valid @RequestBody AnimalRequest request) {
    AnimalResponse created = animalService.addAnimal(request);
    return ResponseEntity.created(URI.create("/api/animals/" + created.getId())).body(created);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<AnimalResponse> updateAnimal(
      @PathVariable Long id, @Valid @RequestBody AnimalRequest request) {
    return ResponseEntity.ok(animalService.updateAnimal(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('OWNER')")
  public ResponseEntity<Void> deleteAnimal(@PathVariable Long id) {
    animalService.deleteAnimal(id);
    return ResponseEntity.noContent().build();
  }
}
