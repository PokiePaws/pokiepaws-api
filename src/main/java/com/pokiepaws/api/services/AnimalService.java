package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.animal.AnimalRequest;
import com.pokiepaws.api.dto.animal.AnimalResponse;
import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnimalService {

  private static final String ANIMAL_NOT_FOUND = "Animal not found";

  private final AnimalRepository animalRepository;
  private final OwnerRepository ownerRepository;

  private Owner getCurrentOwner() {
    var auth = SecurityContextHolder.getContext().getAuthentication();

    if (log.isDebugEnabled()) {
      log.debug("AUTH obj = {}", auth);
      log.debug("AUTH name = {}", auth != null ? auth.getName() : "null");
      log.debug("AUTH authorities = {}", auth != null ? auth.getAuthorities() : "null");
      log.debug("AUTH authenticated = {}", auth != null && auth.isAuthenticated());
    }

    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    String email = auth.getName();

    return ownerRepository
        .findByUserEmail(email)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Owner profile not found for this account (role mismatch?)"));
  }

  @Transactional(readOnly = true)
  public List<AnimalResponse> getMyAnimals() {
    return animalRepository.findAllByOwnerAndActiveTrue(getCurrentOwner()).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AnimalResponse getAnimal(Long id) {
    Animal animal =
        animalRepository
            .findByIdAndOwnerAndActiveTrue(id, getCurrentOwner())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ANIMAL_NOT_FOUND));
    return toResponse(animal);
  }

  @Transactional(readOnly = true)
  public List<AnimalResponse> getAnimalsByOwner(Long ownerId) {
    return animalRepository.findAllByOwnerUserIdAndActiveTrue(ownerId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public AnimalResponse addAnimal(AnimalRequest request) {
    String microchip = cleanMicrochip(request.getMicrochipNumber());
    validateMicrochipUniqueness(microchip, null);

    Animal animal =
        Animal.builder()
            .name(request.getName())
            .species(request.getSpecies())
            .breed(request.getBreed())
            .gender(request.getGender())
            .color(request.getColor())
            .microchipNumber(microchip)
            .weight(request.getWeight())
            .birthDate(request.getBirthDate())
            .notes(request.getNotes())
            .owner(getCurrentOwner())
            .active(true)
            .build();

    return toResponse(animalRepository.save(animal));
  }

  @Transactional
  public AnimalResponse updateAnimal(Long id, AnimalRequest request) {
    Animal animal =
        animalRepository
            .findByIdAndOwnerAndActiveTrue(id, getCurrentOwner())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ANIMAL_NOT_FOUND));

    String microchip = cleanMicrochip(request.getMicrochipNumber());
    validateMicrochipUniqueness(microchip, id);

    animal.setName(request.getName());
    animal.setSpecies(request.getSpecies());
    animal.setBreed(request.getBreed());
    animal.setGender(request.getGender());
    animal.setColor(request.getColor());
    animal.setMicrochipNumber(microchip);
    animal.setWeight(request.getWeight());
    animal.setBirthDate(request.getBirthDate());
    animal.setNotes(request.getNotes());

    return toResponse(animalRepository.save(animal));
  }

  @Transactional
  public void deleteAnimal(Long id) {
    Animal animal =
        animalRepository
            .findByIdAndOwnerAndActiveTrue(id, getCurrentOwner())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ANIMAL_NOT_FOUND));

    animal.setActive(false);
    animalRepository.save(animal);
  }

  private String cleanMicrochip(String microchip) {
    return (microchip != null && microchip.isBlank()) ? null : microchip;
  }

  private void validateMicrochipUniqueness(String microchip, Long currentAnimalId) {
    if (microchip == null) return;

    animalRepository
        .findByMicrochipNumber(microchip)
        .ifPresent(
            existing -> {
              if (!existing.getId().equals(currentAnimalId)) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Microchip number already exists");
              }
            });
  }

  private AnimalResponse toResponse(Animal animal) {
    return AnimalResponse.builder()
        .id(animal.getId())
        .name(animal.getName())
        .species(animal.getSpecies())
        .breed(animal.getBreed())
        .gender(animal.getGender())
        .color(animal.getColor())
        .microchipNumber(animal.getMicrochipNumber())
        .weight(animal.getWeight())
        .birthDate(animal.getBirthDate())
        .notes(animal.getNotes())
        .build();
  }
}
