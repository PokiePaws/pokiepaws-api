package com.pokiepaws.api.config.datainit.seeders;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.pokiepaws.api.config.datainit.dto.AnimalSeedDto;
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
    createAnimalIfMissing(
        AnimalSeedDto.builder()
            .ownerEmail("owner1@pokiepaws.pl")
            .name("Boguś")
            .species("Dog")
            .breed("Maltese")
            .gender(Animal.Gender.MALE)
            .color("White")
            .microchipNumber("123456789123456")
            .weight(3.4)
            .birthDate(LocalDate.of(2011, 1, 12))
            .notes("Friendly, loves water")
            .build());

    createAnimalIfMissing(
        AnimalSeedDto.builder()
            .ownerEmail("owner1@pokiepaws.pl")
            .name("Czesiek")
            .species("Cat")
            .breed("Siamese")
            .gender(Animal.Gender.MALE)
            .color("Brown")
            .microchipNumber("123456789123457")
            .weight(4.5)
            .birthDate(LocalDate.of(2019, 9, 3))
            .notes("Sensitive stomach")
            .build());

    createAnimalIfMissing(
        AnimalSeedDto.builder()
            .ownerEmail("owner2@pokiepaws.pl")
            .name("Milo")
            .species("Cat")
            .breed("British Shorthair")
            .gender(Animal.Gender.MALE)
            .color("Gray")
            .microchipNumber("123456789123458")
            .weight(5.3)
            .birthDate(LocalDate.of(2021, 2, 21))
            .notes("Indoor only")
            .build());

    createAnimalIfMissing(
        AnimalSeedDto.builder()
            .ownerEmail("owner2@pokiepaws.pl")
            .name("Nala")
            .species("Cat")
            .breed("Siamese")
            .gender(Animal.Gender.FEMALE)
            .color("Cream")
            .microchipNumber("123456789123459")
            .weight(3.9)
            .birthDate(LocalDate.of(2022, 7, 1))
            .notes("Very vocal")
            .build());

    createAnimalIfMissing(
        AnimalSeedDto.builder()
            .ownerEmail("owner3@pokiepaws.pl")
            .name("Burek")
            .species("Dog")
            .breed("Mixed")
            .gender(Animal.Gender.MALE)
            .color("White")
            .microchipNumber("123456789123410")
            .weight(12.7)
            .birthDate(LocalDate.of(2018, 11, 18))
            .notes("Adopted, calm")
            .build());

    log.info("Animals seeded.");
  }

  private void createAnimalIfMissing(AnimalSeedDto dto) {
    Owner owner =
        ownerRepository
            .findByUserEmail(dto.getOwnerEmail())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        NOT_FOUND, "Owner not seeded: " + dto.getOwnerEmail()));

    if (dto.getMicrochipNumber() != null
        && animalRepository.findByMicrochipNumber(dto.getMicrochipNumber()).isPresent()) {
      return;
    }

    if (dto.getMicrochipNumber() == null) {
      boolean existsForOwner =
          animalRepository.findAllByOwnerAndActiveTrue(owner).stream()
              .anyMatch(a -> a.getName() != null && a.getName().equalsIgnoreCase(dto.getName()));
      if (existsForOwner) return;
    }

    animalRepository.save(
        Animal.builder()
            .owner(owner)
            .name(dto.getName())
            .species(dto.getSpecies())
            .breed(dto.getBreed())
            .gender(dto.getGender())
            .color(dto.getColor())
            .microchipNumber(dto.getMicrochipNumber())
            .weight(dto.getWeight())
            .birthDate(dto.getBirthDate())
            .notes(dto.getNotes())
            .active(true)
            .build());
  }
}
