package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.dto.visit.CreateVisitRequest;
import com.pokiepaws.api.dto.visit.UpdateVisitMedicalDataRequest;
import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import com.pokiepaws.api.services.VisitService;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class VisitServiceTest {

  @Mock VisitRepository visitRepository;
  @Mock AnimalRepository animalRepository;
  @Mock ClinicRepository clinicRepository;
  @Mock VetRepository vetRepository;
  @Mock OwnerRepository ownerRepository;
  @Mock UserRepository userRepository;

  private VisitService visitService;
  private Owner owner;
  private Animal animal;
  private Clinic clinic;
  private Vet vet;

  @BeforeEach
  void setUp() {
    visitService =
        new VisitService(
            visitRepository,
            animalRepository,
            clinicRepository,
            vetRepository,
            ownerRepository,
            userRepository);

    owner =
        Owner.builder()
            .userId(10L)
            .user(User.builder().id(10L).email("owner@pokiepaws.pl").build())
            .firstName("Anna")
            .lastName("Kowalska")
            .build();
    animal =
        Animal.builder()
            .id(20L)
            .name("Luna")
            .species("Kot")
            .gender(Animal.Gender.FEMALE)
            .owner(owner)
            .active(true)
            .build();
    clinic = Clinic.builder().id(30L).clinicName("PokiePaws Legnica").build();
    vet = Vet.builder().userId(40L).clinic(clinic).firstName("Jan").lastName("Nowak").build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void create_shouldScheduleVisit_whenOwnerAnimalVetClinicAndSlotAreValid() {
    authenticate("owner@pokiepaws.pl");
    CreateVisitRequest request = createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 0));

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.of(clinic));
    when(vetRepository.findById(40L)).thenReturn(Optional.of(vet));
    when(visitRepository.findOverlappingVisits(
            40L, request.getStartsAt(), request.getStartsAt().plusMinutes(30)))
        .thenReturn(List.of());
    when(visitRepository.save(any(Visit.class)))
        .thenAnswer(
            invocation -> {
              Visit visit = invocation.getArgument(0);
              visit.setId(50L);
              return visit;
            });

    var response = visitService.create(request);

    assertThat(response.getId()).isEqualTo(50L);
    assertThat(response.getAnimalId()).isEqualTo(20L);
    assertThat(response.getClinicId()).isEqualTo(30L);
    assertThat(response.getVetUserId()).isEqualTo(40L);
    assertThat(response.getStartsAt()).isEqualTo(LocalDateTime.of(2026, 5, 11, 10, 0));
    assertThat(response.getEndsAt()).isEqualTo(LocalDateTime.of(2026, 5, 11, 10, 30));
    assertThat(response.getStatus()).isEqualTo(VisitStatus.SCHEDULED);

    ArgumentCaptor<Visit> visitCaptor = ArgumentCaptor.forClass(Visit.class);
    verify(visitRepository).save(visitCaptor.capture());
    Visit savedVisit = visitCaptor.getValue();
    assertThat(savedVisit.getAnimal()).isEqualTo(animal);
    assertThat(savedVisit.getClinic()).isEqualTo(clinic);
    assertThat(savedVisit.getVet()).isEqualTo(vet);
    assertThat(savedVisit.getDescription()).isEqualTo("Check");
  }

  @Test
  void create_shouldThrow403_whenAnimalIsNotOwnedByCurrentOwner() {
    authenticate("owner@pokiepaws.pl");

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () -> visitService.create(createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 0))));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(ex.getReason()).isEqualTo("You are not the owner of this animal");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void create_shouldThrow400_whenStartTimeDoesNotMatchThirtyMinuteSlot() {
    authenticate("owner@pokiepaws.pl");
    CreateVisitRequest request = createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 15));

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.of(clinic));
    when(vetRepository.findById(40L)).thenReturn(Optional.of(vet));

    ResponseStatusException ex =
        catchThrowableOfType(ResponseStatusException.class, () -> visitService.create(request));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ex.getReason()).isEqualTo("Start time must align to 30-minute slots");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void create_shouldThrow404_whenClinicDoesNotExist() {
    authenticate("owner@pokiepaws.pl");

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () -> visitService.create(createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 0))));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(ex.getReason()).isEqualTo("Clinic not found");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void create_shouldThrow404_whenVetDoesNotExist() {
    authenticate("owner@pokiepaws.pl");

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.of(clinic));
    when(vetRepository.findById(40L)).thenReturn(Optional.empty());

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () -> visitService.create(createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 0))));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(ex.getReason()).isEqualTo("Vet not found");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void create_shouldThrow400_whenVetDoesNotBelongToSelectedClinic() {
    authenticate("owner@pokiepaws.pl");
    Clinic otherClinic = Clinic.builder().id(31L).clinicName("Other Clinic").build();
    Vet vetFromOtherClinic =
        Vet.builder().userId(40L).clinic(otherClinic).firstName("Jan").lastName("Nowak").build();

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.of(clinic));
    when(vetRepository.findById(40L)).thenReturn(Optional.of(vetFromOtherClinic));

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () -> visitService.create(createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 0))));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ex.getReason()).isEqualTo("Selected vet does not belong to selected clinic");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void create_shouldThrow400_whenStartTimeIsOutsideWorkingHours() {
    authenticate("owner@pokiepaws.pl");
    CreateVisitRequest request = createVisitRequest(LocalDateTime.of(2026, 5, 11, 8, 30));

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.of(clinic));
    when(vetRepository.findById(40L)).thenReturn(Optional.of(vet));

    ResponseStatusException ex =
        catchThrowableOfType(ResponseStatusException.class, () -> visitService.create(request));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ex.getReason()).isEqualTo("Selected time is outside working hours");
  }

  @Test
  void create_shouldThrow409_whenSlotIsAlreadyTaken() {
    authenticate("owner@pokiepaws.pl");
    CreateVisitRequest request = createVisitRequest(LocalDateTime.of(2026, 5, 11, 10, 0));

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(animalRepository.findByIdAndOwnerAndActiveTrue(20L, owner))
        .thenReturn(Optional.of(animal));
    when(clinicRepository.findById(30L)).thenReturn(Optional.of(clinic));
    when(vetRepository.findById(40L)).thenReturn(Optional.of(vet));
    when(visitRepository.findOverlappingVisits(
            40L, request.getStartsAt(), request.getStartsAt().plusMinutes(30)))
        .thenReturn(List.of(visit(99L, VisitStatus.SCHEDULED)));

    ResponseStatusException ex =
        catchThrowableOfType(ResponseStatusException.class, () -> visitService.create(request));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(ex.getReason()).isEqualTo("Selected slot is already taken");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void cancelForCurrentOwner_shouldMarkVisitCancelled() {
    authenticate("owner@pokiepaws.pl");
    Visit visit = visit(50L, VisitStatus.SCHEDULED);

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));

    var response = visitService.cancelForCurrentOwner(50L);

    assertThat(response.getStatus()).isEqualTo(VisitStatus.CANCELLED);
    assertThat(visit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
    verify(visitRepository).save(visit);
  }

  @Test
  void cancelForCurrentOwner_shouldThrow403_whenVisitBelongsToDifferentOwner() {
    authenticate("owner@pokiepaws.pl");
    Owner otherOwner =
        Owner.builder()
            .userId(11L)
            .user(User.builder().id(11L).email("other@pokiepaws.pl").build())
            .build();
    Animal foreignAnimal =
        Animal.builder()
            .id(21L)
            .name("Figa")
            .species("Pies")
            .gender(Animal.Gender.FEMALE)
            .owner(otherOwner)
            .active(true)
            .build();
    Visit foreignVisit = visit(51L, VisitStatus.SCHEDULED);
    foreignVisit.setAnimal(foreignAnimal);

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(visitRepository.findById(51L)).thenReturn(Optional.of(foreignVisit));

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class, () -> visitService.cancelForCurrentOwner(51L));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(ex.getReason()).isEqualTo("You cannot cancel this visit");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void getByIdForCurrentOwner_shouldThrow403_whenVisitBelongsToDifferentOwner() {
    authenticate("owner@pokiepaws.pl");
    Owner otherOwner =
        Owner.builder()
            .userId(11L)
            .user(User.builder().id(11L).email("other@pokiepaws.pl").build())
            .build();
    Animal foreignAnimal =
        Animal.builder()
            .id(21L)
            .name("Figa")
            .species("Pies")
            .gender(Animal.Gender.FEMALE)
            .owner(otherOwner)
            .active(true)
            .build();
    Visit foreignVisit = visit(51L, VisitStatus.SCHEDULED);
    foreignVisit.setAnimal(foreignAnimal);

    when(ownerRepository.findByUserEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(owner));
    when(visitRepository.findById(51L)).thenReturn(Optional.of(foreignVisit));

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class, () -> visitService.getByIdForCurrentOwner(51L));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(ex.getReason()).isEqualTo("Access denied");
  }

  @Test
  void getMyVisitsInRange_shouldThrow400_whenFromIsAfterTo() {
    authenticate("owner@pokiepaws.pl");

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () ->
                visitService.getMyVisitsInRange(
                    LocalDate.of(2026, 5, 12), LocalDate.of(2026, 5, 11)));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ex.getReason()).isEqualTo("'from' must be <= 'to'");
  }

  @Test
  void updateMedicalData_shouldSaveDiagnosisAndRecommendationsForCurrentVet() {
    authenticate("vet@pokiepaws.pl");
    User vetUser = User.builder().id(40L).email("vet@pokiepaws.pl").build();
    Visit visit = visit(50L, VisitStatus.SCHEDULED);
    UpdateVisitMedicalDataRequest request = new UpdateVisitMedicalDataRequest();
    request.setDisease("Zapalenie ucha");
    request.setDiagnosis("Stan zapalny bez goraczki");
    request.setRecommendations("Krople 2x dziennie");

    when(userRepository.findByEmail("vet@pokiepaws.pl")).thenReturn(Optional.of(vetUser));
    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(visitRepository.save(visit)).thenReturn(visit);

    var response = visitService.updateMedicalData(50L, request);

    assertThat(response.getDisease()).isEqualTo("Zapalenie ucha");
    assertThat(response.getDiagnosis()).isEqualTo("Stan zapalny bez goraczki");
    assertThat(response.getRecommendations()).isEqualTo("Krople 2x dziennie");
    verify(visitRepository).save(visit);
  }

  @Test
  void updateMedicalData_shouldThrow400_whenVisitIsCancelled() {
    authenticate("vet@pokiepaws.pl");
    User vetUser = User.builder().id(40L).email("vet@pokiepaws.pl").build();

    when(userRepository.findByEmail("vet@pokiepaws.pl")).thenReturn(Optional.of(vetUser));
    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit(50L, VisitStatus.CANCELLED)));

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () -> visitService.updateMedicalData(50L, new UpdateVisitMedicalDataRequest()));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(ex.getReason()).isEqualTo("Cannot update cancelled visit");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  @Test
  void updateMedicalData_shouldThrow403_whenCurrentVetIsNotAssignedToVisit() {
    authenticate("other-vet@pokiepaws.pl");
    User otherVet = User.builder().id(41L).email("other-vet@pokiepaws.pl").build();

    when(userRepository.findByEmail("other-vet@pokiepaws.pl")).thenReturn(Optional.of(otherVet));
    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit(50L, VisitStatus.SCHEDULED)));

    ResponseStatusException ex =
        catchThrowableOfType(
            ResponseStatusException.class,
            () -> visitService.updateMedicalData(50L, new UpdateVisitMedicalDataRequest()));

    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(ex.getReason()).isEqualTo("You are not the vet for this visit");
    verify(visitRepository, never()).save(any(Visit.class));
  }

  private void authenticate(String email) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(email, "password", List.of()));
  }

  private CreateVisitRequest createVisitRequest(LocalDateTime startsAt) {
    CreateVisitRequest request = new CreateVisitRequest();
    request.setAnimalId(20L);
    request.setClinicId(30L);
    request.setVetUserId(40L);
    request.setStartsAt(startsAt);
    request.setDescription("Check");
    return request;
  }

  private Visit visit(Long id, VisitStatus status) {
    return Visit.builder()
        .id(id)
        .animal(animal)
        .clinic(clinic)
        .vet(vet)
        .startsAt(LocalDateTime.of(2026, 5, 11, 10, 0))
        .endsAt(LocalDateTime.of(2026, 5, 11, 10, 30))
        .description("Check")
        .status(status)
        .used(false)
        .build();
  }
}
