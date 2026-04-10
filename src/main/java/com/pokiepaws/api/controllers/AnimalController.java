package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.AnimalRequest;
import com.pokiepaws.api.dto.AnimalResponse;
import com.pokiepaws.api.services.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/animals")
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

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AnimalResponse> addAnimal(
            @Valid @RequestBody AnimalRequest request) {
        return ResponseEntity.ok(animalService.addAnimal(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AnimalResponse> updateAnimal(
            @PathVariable Long id,
            @Valid @RequestBody AnimalRequest request) {
        return ResponseEntity.ok(animalService.updateAnimal(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteAnimal(@PathVariable Long id) {
        animalService.deleteAnimal(id);
        return ResponseEntity.noContent().build();
    }
}