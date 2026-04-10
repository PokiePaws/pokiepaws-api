package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.AnimalRequest;
import com.pokiepaws.api.dto.AnimalResponse;
import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> getMyAnimals() {
        return animalRepository.findAllByOwnerAndActiveTrue(getCurrentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse getAnimal(Long id) {
        Animal animal = animalRepository.findByIdAndOwner(id, getCurrentUser())
                .orElseThrow(() -> new RuntimeException("Animal not found"));
        return toResponse(animal);
    }

    @Transactional(readOnly = true)
    public List<AnimalResponse> getAnimalsByOwner(Long ownerId) {
        return animalRepository.findAllByOwnerIdAndActiveTrue(ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AnimalResponse addAnimal(AnimalRequest request) {
        validateMicrochipUniqueness(request.getMicrochipNumber(), null);

        Animal animal = Animal.builder()
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .gender(request.getGender())
                .color(request.getColor())
                .microchipNumber(cleanMicrochip(request.getMicrochipNumber()))
                .weight(request.getWeight())
                .birthDate(request.getBirthDate())
                .notes(request.getNotes())
                .owner(getCurrentUser())
                .active(true)
                .build();

        return toResponse(animalRepository.save(animal));
    }

    @Transactional
    public AnimalResponse updateAnimal(Long id, AnimalRequest request) {
        Animal animal = animalRepository.findByIdAndOwner(id, getCurrentUser())
                .orElseThrow(() -> new RuntimeException("Animal not found"));

        validateMicrochipUniqueness(request.getMicrochipNumber(), id);

        animal.setName(request.getName());
        animal.setSpecies(request.getSpecies());
        animal.setBreed(request.getBreed());
        animal.setGender(request.getGender());
        animal.setColor(request.getColor());
        animal.setMicrochipNumber(cleanMicrochip(request.getMicrochipNumber()));
        animal.setWeight(request.getWeight());
        animal.setBirthDate(request.getBirthDate());
        animal.setNotes(request.getNotes());

        return toResponse(animalRepository.save(animal));
    }

    @Transactional
    public void deleteAnimal(Long id) {
        Animal animal = animalRepository.findByIdAndOwner(id, getCurrentUser())
                .orElseThrow(() -> new RuntimeException("Animal not founde"));

        animal.setActive(false);
        animalRepository.save(animal);
    }

    private String cleanMicrochip(String microchip) {
        return (microchip != null && microchip.isBlank()) ? null : microchip;
    }

    private void validateMicrochipUniqueness(String microchip, Long currentAnimalId) {
        if (microchip != null && !microchip.isBlank()) {
            animalRepository.findByMicrochipNumber(microchip).ifPresent(existing -> {
                if (!existing.getId().equals(currentAnimalId)) {
                    throw new RuntimeException("Invalid animal chip number");
                }
            });
        }
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