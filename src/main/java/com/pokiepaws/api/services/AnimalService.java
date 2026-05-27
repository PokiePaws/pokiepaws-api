package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.animal.AnimalRequest;
import com.pokiepaws.api.dto.animal.AnimalResponse;
import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.VetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
  private final VetRepository vetRepository;

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
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (hasRole(auth, "ROLE_VET") || hasRole(auth, "ROLE_ADMIN")) {
      Animal animal =
          animalRepository
              .findByIdAndActiveTrue(id)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, ANIMAL_NOT_FOUND));
      if (hasRole(auth, "ROLE_ADMIN") || currentVetCanAccessAnimal(auth.getName(), animal)) {
        return toResponse(animal);
      }
      throw new AccessDeniedException("Vet cannot access patient from another clinic");
    }

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

  @Transactional(readOnly = true)
  public List<AnimalResponse> getPatientsByClinic(Long clinicId) {
    ensureCurrentUserCanAccessClinicPatients(clinicId);
    return animalRepository.findDistinctActivePatientsByClinicId(clinicId).stream()
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

  private void ensureCurrentUserCanAccessClinicPatients(Long clinicId) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (hasRole(auth, "ROLE_ADMIN")) {
      return;
    }

    var vet =
        vetRepository
            .findByUserEmail(auth.getName())
            .orElseThrow(() -> new AccessDeniedException("Vet profile not found"));

    if (vet.getClinic() == null || !clinicId.equals(vet.getClinic().getId())) {
      throw new AccessDeniedException("Vet cannot access patients from another clinic");
    }
  }

  private boolean currentVetCanAccessAnimal(String email, Animal animal) {
    var vet =
        vetRepository
            .findByUserEmail(email)
            .orElseThrow(() -> new AccessDeniedException("Vet profile not found"));

    Long clinicId = vet.getClinic() != null ? vet.getClinic().getId() : null;
    if (clinicId == null) {
      return false;
    }

    return animalRepository.findDistinctActivePatientsByClinicId(clinicId).stream()
        .anyMatch(patient -> patient.getId().equals(animal.getId()));
  }

  private boolean hasRole(org.springframework.security.core.Authentication auth, String role) {
    return auth != null
        && auth.getAuthorities().stream()
            .anyMatch(authority -> role.equals(authority.getAuthority()));
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
