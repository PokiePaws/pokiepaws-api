package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.dto.animal.AnimalRequest;
import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.services.AnimalService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

  @Mock AnimalRepository animalRepository;
  @Mock OwnerRepository ownerRepository;
  @Mock VetRepository vetRepository;

  private AnimalService animalService;
  private Owner owner;
  private static final String OWNER_EMAIL = "gabriela@pokiepaws.pl";

  @BeforeEach
  void setUp() {
    animalService = new AnimalService(animalRepository, ownerRepository, vetRepository);
    owner =
        Owner.builder()
            .userId(10L)
            .user(User.builder().id(10L).email(OWNER_EMAIL).build())
            .firstName("Gabriela")
            .lastName("Grabarska")
            .street("Zielona")
            .houseNumber("10")
            .apartmentNumber("2")
            .postalCode("59-220")
            .city("Legnica")
            .country("Poland")
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getMyAnimals_shouldReturnOnlyCurrentOwnersActiveAnimals() {
    authenticate();
    Animal animal = animal(1L, "Luna", "Cat", "982000000000001");

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findAllByOwnerAndActiveTrue(owner)).thenReturn(List.of(animal));

    var result = animalService.getMyAnimals();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(1L);
    assertThat(result.getFirst().getName()).isEqualTo("Luna");
    assertThat(result.getFirst().getMicrochipNumber()).isEqualTo("982000000000001");
  }

  @Test
  void getAnimal_shouldThrow404_whenAnimalDoesNotBelongToCurrentOwnerOrIsInactive() {
    authenticate();

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(99L, owner)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> animalService.getAnimal(99L))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Animal not found");
            });
  }

  @Test
  void getAnimal_shouldReturnAnimal_whenBelongsToCurrentOwnerAndActive() {
    authenticate();
    Animal luna = animal(1L, "Luna", "Cat", "982000000000001");

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(1L, owner)).thenReturn(Optional.of(luna));

    var result = animalService.getAnimal(1L);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Luna");
    assertThat(result.getMicrochipNumber()).isEqualTo("982000000000001");
  }

  @Test
  void getAnimalsByOwner_shouldReturnAnimalResponses_whenOwnerHasActiveAnimals() {
    Long searchOwnerId = 15L;
    Animal figa = animal(7L, "Figa", "Dog", "982000000000999");
    when(animalRepository.findAllByOwnerUserIdAndActiveTrue(searchOwnerId))
        .thenReturn(List.of(figa));

    var result = animalService.getAnimalsByOwner(searchOwnerId);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getId()).isEqualTo(7L);
    assertThat(result.getFirst().getName()).isEqualTo("Figa");
  }

  @Test
  void updateAnimal_shouldUpdateFields_whenValidRequestAndMicrochipUnchanged() {
    authenticate();
    Animal existingAnimal = animal(1L, "Luna", "Cat", "982000000000001");

    AnimalRequest request = validAnimalRequest();
    request.setName("Luna Updated");
    request.setWeight(5.0);
    request.setMicrochipNumber("982000000000001");

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(1L, owner))
        .thenReturn(Optional.of(existingAnimal));
    when(animalRepository.findByMicrochipNumber("982000000000001"))
        .thenReturn(Optional.of(existingAnimal));
    when(animalRepository.save(any(Animal.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = animalService.updateAnimal(1L, request);

    assertThat(result.getName()).isEqualTo("Luna Updated");
    assertThat(result.getWeight()).isEqualTo(5.0);
    assertThat(result.getMicrochipNumber()).isEqualTo("982000000000001");

    verify(animalRepository).save(existingAnimal);
  }

  @Test
  void updateAnimal_shouldAllowRemovingMicrochip_bySettingItToBlank() {
    authenticate();
    Animal existingAnimal = animal(1L, "Luna", "Cat", "982000000000001");

    AnimalRequest request = validAnimalRequest();
    request.setMicrochipNumber("   ");

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(1L, owner))
        .thenReturn(Optional.of(existingAnimal));
    when(animalRepository.save(any(Animal.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = animalService.updateAnimal(1L, request);

    assertThat(result.getMicrochipNumber()).isNull();
    verify(animalRepository, never()).findByMicrochipNumber(any());
    verify(animalRepository).save(existingAnimal);
  }

  @Test
  void addAnimal_shouldAssignCurrentOwnerAndNormalizeBlankMicrochip() {
    authenticate();
    AnimalRequest request = validAnimalRequest();
    request.setMicrochipNumber("   ");

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.save(any(Animal.class)))
        .thenAnswer(
            invocation -> {
              Animal saved = invocation.getArgument(0);
              saved.setId(20L);
              return saved;
            });

    var result = animalService.addAnimal(request);

    assertThat(result.getId()).isEqualTo(20L);
    assertThat(result.getMicrochipNumber()).isNull();

    ArgumentCaptor<Animal> savedAnimalCaptor = ArgumentCaptor.forClass(Animal.class);
    verify(animalRepository).save(savedAnimalCaptor.capture());
    Animal savedAnimal = savedAnimalCaptor.getValue();
    assertThat(savedAnimal.getOwner()).isEqualTo(owner);
    assertThat(savedAnimal.isActive()).isTrue();
  }

  @Test
  void addAnimal_shouldThrow409_whenMicrochipAlreadyExists() {
    authenticate();
    AnimalRequest request = validAnimalRequest();
    request.setMicrochipNumber("982000000000001");

    when(animalRepository.findByMicrochipNumber("982000000000001"))
        .thenReturn(Optional.of(animal(7L, "Figa", "Dog", "982000000000001")));

    assertThatThrownBy(() -> animalService.addAnimal(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(rse.getReason()).isEqualTo("Microchip number already exists");
            });

    verify(animalRepository, never()).save(any(Animal.class));
  }

  @Test
  void deleteAnimal_shouldSoftDeleteOwnedAnimal() {
    authenticate();
    Animal animal = animal(3L, "Mika", "Cat", null);

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(3L, owner)).thenReturn(Optional.of(animal));

    animalService.deleteAnimal(3L);

    assertThat(animal.isActive()).isFalse();
    verify(animalRepository).save(animal);
  }

  @Test
  void getMyAnimals_shouldThrow401_whenUserIsAnonymous() {
    SecurityContextHolder.clearContext();

    assertThatThrownBy(() -> animalService.getMyAnimals())
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
              assertThat(rse.getReason()).isEqualTo("Not authenticated");
            });
  }

  @Test
  void anySecuredMethod_shouldThrow403_whenOwnerProfileDoesNotExist() {
    authenticate();
    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> animalService.getMyAnimals())
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
              assertThat(rse.getReason()).contains("Owner profile not found");
            });
  }

  @Test
  void updateAnimal_shouldThrow404_whenAnimalDoesNotExistOrDoesNotBelongToOwner() {
    authenticate();
    AnimalRequest request = validAnimalRequest();

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(99L, owner)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> animalService.updateAnimal(99L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Animal not found");
            });

    verify(animalRepository, never()).save(any(Animal.class));
  }

  @Test
  void updateAnimal_shouldThrow409_whenMicrochipBelongsToAnotherAnimal() {
    authenticate();

    Animal luna = animal(1L, "Luna", "Cat", "982000000000001");
    Animal figa = animal(7L, "Figa", "Dog", "982000000000999");

    AnimalRequest request = validAnimalRequest();
    request.setMicrochipNumber("982000000000999");

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(1L, owner)).thenReturn(Optional.of(luna));
    when(animalRepository.findByMicrochipNumber("982000000000999")).thenReturn(Optional.of(figa));

    assertThatThrownBy(() -> animalService.updateAnimal(1L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(rse.getReason()).isEqualTo("Microchip number already exists");
            });

    verify(animalRepository, never()).save(any(Animal.class));
  }

  @Test
  void deleteAnimal_shouldThrow404_whenAnimalDoesNotExistOrDoesNotBelongToOwner() {
    authenticate();

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(99L, owner)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> animalService.deleteAnimal(99L))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Animal not found");
            });

    verify(animalRepository, never()).save(any(Animal.class));
  }

  @Test
  void getAnimalsByOwner_shouldReturnEmptyList_whenOwnerHasNoActiveAnimals() {
    Long searchOwnerId = 15L;
    when(animalRepository.findAllByOwnerUserIdAndActiveTrue(searchOwnerId)).thenReturn(List.of());

    var result = animalService.getAnimalsByOwner(searchOwnerId);

    assertThat(result).isEmpty();
  }

  private void authenticate() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(OWNER_EMAIL, "password", List.of()));
  }

  private AnimalRequest validAnimalRequest() {
    return AnimalRequest.builder()
        .name("Luna")
        .species("Cat")
        .breed("European")
        .gender(Animal.Gender.FEMALE)
        .color("Black")
        .microchipNumber("982000000000001")
        .weight(4.2)
        .birthDate(LocalDate.of(2022, 5, 10))
        .notes("Chicken allergy")
        .build();
  }

  private Animal animal(Long id, String name, String species, String microchip) {
    return Animal.builder()
        .id(id)
        .name(name)
        .species(species)
        .gender(Animal.Gender.FEMALE)
        .microchipNumber(microchip)
        .owner(owner)
        .active(true)
        .build();
  }
}
