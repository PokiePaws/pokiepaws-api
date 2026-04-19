package com.pokiepaws.api.config.seeders;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class AnimalsSeeder implements Seeder {

  private final AnimalRepository animalRepository;
  private final OwnerRepository ownerRepository;

  @Override
  public int order() {
    return 25;
  }

  @Override
  @Transactional
  public void seed() {
    Owner owner1 = getOwnerOrThrow("owner1@pokiepaws.pl");
    Owner owner2 = getOwnerOrThrow("owner2@pokiepaws.pl");
    Owner owner3 = getOwnerOrThrow("owner3@pokiepaws.pl");

    createAnimalIfMissing(
        owner1,
        "Boguś",
        "Dog",
        "Maltese",
        Animal.Gender.MALE,
        "White",
        "123456789123456",
        3.4,
        LocalDate.of(2011, 1, 12),
        "Friendly, loves water");

    createAnimalIfMissing(
        owner1,
        "Czesiek",
        "Cat",
        "Siamese",
        Animal.Gender.MALE,
        "Brown",
        "123456789123457",
        4.5,
        LocalDate.of(2019, 9, 3),
        "Sensitive stomach");

    createAnimalIfMissing(
        owner2,
        "Milo",
        "Cat",
        "British Shorthair",
        Animal.Gender.MALE,
        "Gray",
        "123456789123458",
        5.3,
        LocalDate.of(2021, 2, 21),
        "Indoor only");

    createAnimalIfMissing(
        owner2,
        "Nala",
        "Cat",
        "Siamese",
        Animal.Gender.FEMALE,
        "Cream",
        "123456789123459",
        3.9,
        LocalDate.of(2022, 7, 1),
        "Very vocal");

    createAnimalIfMissing(
        owner3,
        "Burek",
        "Dog",
        "Mixed",
        Animal.Gender.MALE,
        "White",
        "123456789123410",
        12.7,
        LocalDate.of(2018, 11, 18),
        "Adopted, calm");

    log.info("Animals seeded.");
  }

  private Owner getOwnerOrThrow(String ownerEmail) {
    return ownerRepository
        .findByUserEmail(ownerEmail)
        .orElseThrow(
            () -> new ResponseStatusException(NOT_FOUND, "Owner not seeded: " + ownerEmail));
  }

  private void createAnimalIfMissing(
      Owner owner,
      String name,
      String species,
      String breed,
      Animal.Gender gender,
      String color,
      String microchipNumber,
      Double weight,
      LocalDate birthDate,
      String notes) {

    if (microchipNumber != null
        && animalRepository.findByMicrochipNumber(microchipNumber).isPresent()) {
      return;
    }
    if (microchipNumber == null) {
      boolean existsForOwner =
          animalRepository.findAllByOwnerAndActiveTrue(owner).stream()
              .anyMatch(a -> a.getName() != null && a.getName().equalsIgnoreCase(name));
      if (existsForOwner) return;
    }

    animalRepository.save(
        Animal.builder()
            .owner(owner)
            .name(name)
            .species(species)
            .breed(breed)
            .gender(gender)
            .color(color)
            .microchipNumber(microchipNumber)
            .weight(weight)
            .birthDate(birthDate)
            .notes(notes)
            .active(true)
            .build());
  }
}
