package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.config.properties.VisitScheduleProperties;
import com.pokiepaws.api.dto.visit.CreateVisitRequest;
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
import com.pokiepaws.api.services.OwnerNotificationService;
import com.pokiepaws.api.services.RealtimeNotificationService;
import com.pokiepaws.api.services.VisitService;
import com.pokiepaws.api.validators.VisitValidator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

  @Mock VisitRepository visitRepository;
  @Mock AnimalRepository animalRepository;
  @Mock ClinicRepository clinicRepository;
  @Mock VetRepository vetRepository;
  @Mock OwnerRepository ownerRepository;
  @Mock UserRepository userRepository;
  @Mock RealtimeNotificationService realtimeNotificationService;
  @Mock OwnerNotificationService ownerNotificationService;

  private VisitService visitService;
  private Owner owner;
  private Animal animal;
  private Clinic clinic;
  private Vet vet;
  private VisitValidator visitValidator;
  private Clock clock;

  @BeforeEach
  void setUp() {
    VisitScheduleProperties visitScheduleProperties = new VisitScheduleProperties();
    visitValidator = new VisitValidator(visitScheduleProperties, visitRepository);
    clock = Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneId.of("UTC"));

    visitService =
        new VisitService(
            visitRepository,
            animalRepository,
            clinicRepository,
            vetRepository,
            ownerRepository,
            userRepository,
            realtimeNotificationService,
            ownerNotificationService,
            visitValidator,
            clock);

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

    verify(ownerNotificationService, never()).visitConfirmed(any(Visit.class));
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
