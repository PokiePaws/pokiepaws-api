package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.pokiepaws.api.dto.prescription.CreatePrescriptionRequest;
import com.pokiepaws.api.dto.prescription.PrescriptionItemRequest;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.ClinicStockItemRepository;
import com.pokiepaws.api.repositories.PrescriptionRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import com.pokiepaws.api.repositories.WarehouseStockItemRepository;
import com.pokiepaws.api.services.OwnerNotificationService;
import com.pokiepaws.api.services.PrescriptionService;
import com.pokiepaws.api.services.RealtimeNotificationService;
import com.pokiepaws.api.validators.PrescriptionValidator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

  @Mock VisitRepository visitRepository;
  @Mock PrescriptionRepository prescriptionRepository;
  @Mock WarehouseStockItemRepository warehouseStockItemRepository;
  @Mock ClinicStockItemRepository clinicStockItemRepository;
  @Mock UserRepository userRepository;
  @Mock RealtimeNotificationService realtimeNotificationService;
  @Mock OwnerNotificationService ownerNotificationService;

  private PrescriptionService prescriptionService;
  private Clinic clinic;
  private Vet vet;
  private Visit visit;
  private WarehouseStockItem stockItem;
  private ClinicStockItem stock;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(Instant.parse("2026-05-10T10:00:00Z"), ZoneId.of("UTC"));
    PrescriptionValidator prescriptionValidator = new PrescriptionValidator(prescriptionRepository);
    prescriptionService =
        new PrescriptionService(
            clock,
            visitRepository,
            prescriptionRepository,
            warehouseStockItemRepository,
            clinicStockItemRepository,
            userRepository,
            realtimeNotificationService,
            ownerNotificationService,
            prescriptionValidator);

    clinic = Clinic.builder().id(30L).clinicName("PokiePaws Legnica").build();
    vet = Vet.builder().userId(40L).clinic(clinic).firstName("John").lastName("Smith").build();
    Owner owner = Owner.builder().userId(10L).user(User.builder().id(10L).build()).build();
    Animal animal =
        Animal.builder()
            .id(20L)
            .name("Luna")
            .species("Cat")
            .gender(Animal.Gender.FEMALE)
            .owner(owner)
            .active(true)
            .build();
    visit =
        Visit.builder()
            .id(50L)
            .animal(animal)
            .clinic(clinic)
            .vet(vet)
            .startsAt(LocalDateTime.of(2026, 5, 11, 10, 0))
            .endsAt(LocalDateTime.of(2026, 5, 11, 10, 30))
            .status(VisitStatus.SCHEDULED)
            .build();
    stockItem =
        WarehouseStockItem.builder().id(70L).name("Antibiotic").unit("package").amount(100).build();
    stock =
        ClinicStockItem.builder()
            .id(80L)
            .clinic(clinic)
            .stockItem(stockItem)
            .quantityPackages(5)
            .build();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void createForVisit_shouldCreatePrescriptionAndDecreaseClinicStock() {
    authenticate("vet@pokiepaws.pl", "ROLE_VET");
    User currentVet = User.builder().id(40L).email("vet@pokiepaws.pl").role(Role.VET).build();
    CreatePrescriptionRequest request = prescriptionRequest(2);

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(false);
    when(userRepository.findByEmail("vet@pokiepaws.pl")).thenReturn(Optional.of(currentVet));
    when(warehouseStockItemRepository.findById(70L)).thenReturn(Optional.of(stockItem));
    when(clinicStockItemRepository.findByClinicIdAndStockItemId(30L, 70L))
        .thenReturn(Optional.of(stock));
    when(prescriptionRepository.save(any(Prescription.class)))
        .thenAnswer(
            invocation -> {
              Prescription prescription = invocation.getArgument(0);
              prescription.setId(90L);
              prescription.getItems().getFirst().setId(91L);
              return prescription;
            });

    var response = prescriptionService.createForVisit(50L, request);

    assertThat(response.getId()).isEqualTo(90L);
    assertThat(response.getVisitId()).isEqualTo(50L);
    assertThat(response.getVetUserId()).isEqualTo(40L);
    assertThat(response.getCreationDate()).isEqualTo(LocalDate.of(2026, 5, 10));
    assertThat(response.getItems()).hasSize(1);
    assertThat(response.getItems().getFirst().getProductName()).isEqualTo("Antibiotic");

    assertThat(stock.getQuantityPackages()).isEqualTo(3);
    verify(clinicStockItemRepository).save(stock);

    ArgumentCaptor<Prescription> prescriptionCaptor = ArgumentCaptor.forClass(Prescription.class);
    verify(prescriptionRepository).save(prescriptionCaptor.capture());
    assertThat(prescriptionCaptor.getValue().getItems().getFirst().getPrescription())
        .isEqualTo(prescriptionCaptor.getValue());
  }

  @Test
  void createForVisit_shouldThrow409_whenPrescriptionAlreadyExists() {
    CreatePrescriptionRequest request = prescriptionRequest(1);
    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(true);

    assertThatThrownBy(() -> prescriptionService.createForVisit(50L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(rse.getReason()).isEqualTo("Prescription already exists for this visit");
            });

    verify(prescriptionRepository, never()).save(any(Prescription.class));
  }

  @Test
  void createForVisit_shouldThrow404_whenVisitDoesNotExist() {
    CreatePrescriptionRequest request = prescriptionRequest(1);
    when(visitRepository.findById(404L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> prescriptionService.createForVisit(404L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Visit not found");
            });

    verify(prescriptionRepository, never()).save(any(Prescription.class));
  }

  @Test
  void createForVisit_shouldThrow400_whenVisitHasNoAssignedVet() {
    CreatePrescriptionRequest request = prescriptionRequest(1);
    Visit visitWithoutVet = visit;
    visitWithoutVet.setVet(null);

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visitWithoutVet));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(false);

    assertThatThrownBy(() -> prescriptionService.createForVisit(50L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("Visit has no assigned vet");
            });
  }

  @Test
  void createForVisit_shouldThrow403_whenCurrentVetIsNotAssignedToVisit() {
    CreatePrescriptionRequest request = prescriptionRequest(1);
    authenticate("other-vet@pokiepaws.pl", "ROLE_VET");
    User otherVet = User.builder().id(41L).email("other-vet@pokiepaws.pl").role(Role.VET).build();

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(false);
    when(userRepository.findByEmail("other-vet@pokiepaws.pl")).thenReturn(Optional.of(otherVet));

    assertThatThrownBy(() -> prescriptionService.createForVisit(50L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
              assertThat(rse.getReason()).isEqualTo("You are not assigned vet for this visit");
            });
  }

  @Test
  void createForVisit_shouldThrow400_whenClinicStockIsInsufficient() {
    CreatePrescriptionRequest request = prescriptionRequest(6);
    authenticate("vet@pokiepaws.pl", "ROLE_VET");
    User currentVet = User.builder().id(40L).email("vet@pokiepaws.pl").role(Role.VET).build();

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(false);
    when(userRepository.findByEmail("vet@pokiepaws.pl")).thenReturn(Optional.of(currentVet));
    when(warehouseStockItemRepository.findById(70L)).thenReturn(Optional.of(stockItem));
    when(clinicStockItemRepository.findByClinicIdAndStockItemId(30L, 70L))
        .thenReturn(Optional.of(stock));

    assertThatThrownBy(() -> prescriptionService.createForVisit(50L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).contains("Not enough stock for stockItemId=70");
            });
  }

  @Test
  void createForVisit_shouldThrow404_whenProductDoesNotExist() {
    CreatePrescriptionRequest request = prescriptionRequest(1);
    authenticate("vet@pokiepaws.pl", "ROLE_VET");
    User currentVet = User.builder().id(40L).email("vet@pokiepaws.pl").role(Role.VET).build();

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(false);
    when(userRepository.findByEmail("vet@pokiepaws.pl")).thenReturn(Optional.of(currentVet));
    when(warehouseStockItemRepository.findById(70L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> prescriptionService.createForVisit(50L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Product not found: 70");
            });

    verify(clinicStockItemRepository, never()).save(any());
    verify(prescriptionRepository, never()).save(any(Prescription.class));
  }

  @Test
  void createForVisit_shouldThrow400_whenProductIsNotAvailableInClinicStock() {
    CreatePrescriptionRequest request = prescriptionRequest(1);
    authenticate("vet@pokiepaws.pl", "ROLE_VET");
    User currentVet = User.builder().id(40L).email("vet@pokiepaws.pl").role(Role.VET).build();

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(prescriptionRepository.existsByVisitId(50L)).thenReturn(false);
    when(userRepository.findByEmail("vet@pokiepaws.pl")).thenReturn(Optional.of(currentVet));
    when(warehouseStockItemRepository.findById(70L)).thenReturn(Optional.of(stockItem));
    when(clinicStockItemRepository.findByClinicIdAndStockItemId(30L, 70L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> prescriptionService.createForVisit(50L, request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("Product not available in clinic stock: 70");
            });

    verify(clinicStockItemRepository, never()).save(any());
    verify(prescriptionRepository, never()).save(any(Prescription.class));
  }

  @Test
  void getForVisitForCurrentOwner_shouldReturnPrescriptionOnlyForOwnVisit() {
    authenticate("owner@pokiepaws.pl", "ROLE_OWNER");
    User currentOwner = User.builder().id(10L).email("owner@pokiepaws.pl").role(Role.OWNER).build();
    Prescription prescription = prescription();

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(userRepository.findByEmail("owner@pokiepaws.pl")).thenReturn(Optional.of(currentOwner));
    when(prescriptionRepository.findByVisitId(50L)).thenReturn(Optional.of(prescription));

    var response = prescriptionService.getForVisitForCurrentOwner(50L);

    assertThat(response.getVisitId()).isEqualTo(50L);
    assertThat(response.getItems().getFirst().getProductId()).isEqualTo(70L);
  }

  @Test
  void getForVisitForCurrentOwner_shouldThrow403ForForeignVisit() {
    authenticate("other-owner@pokiepaws.pl", "ROLE_OWNER");
    User otherOwner =
        User.builder().id(11L).email("other-owner@pokiepaws.pl").role(Role.OWNER).build();
    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(userRepository.findByEmail("other-owner@pokiepaws.pl"))
        .thenReturn(Optional.of(otherOwner));

    assertThatThrownBy(() -> prescriptionService.getForVisitForCurrentOwner(50L))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
              assertThat(rse.getReason()).isEqualTo("This visit is not yours");
            });
  }

  @Test
  void getForVisit_shouldThrow404_whenPrescriptionDoesNotExist() {
    when(prescriptionRepository.findByVisitId(50L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> prescriptionService.getForVisit(50L))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
              assertThat(rse.getReason()).isEqualTo("Prescription not found for this visit");
            });
  }

  @Test
  void getForVisitForCurrentVetOrAdmin_shouldAllowAdminForAnyVisit() {
    authenticate("admin@pokiepaws.pl", "ROLE_ADMIN");
    User admin = User.builder().id(1L).email("admin@pokiepaws.pl").role(Role.ADMIN).build();

    when(visitRepository.findById(50L)).thenReturn(Optional.of(visit));
    when(userRepository.findByEmail("admin@pokiepaws.pl")).thenReturn(Optional.of(admin));
    when(prescriptionRepository.findByVisitId(50L)).thenReturn(Optional.of(prescription()));

    var response = prescriptionService.getForVisitForCurrentVetOrAdmin(50L);

    assertThat(response.getClinicId()).isEqualTo(30L);
  }

  private void authenticate(String email, String authority) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                email, "password", List.of(new SimpleGrantedAuthority(authority))));
  }

  private CreatePrescriptionRequest prescriptionRequest(int quantity) {
    PrescriptionItemRequest item = new PrescriptionItemRequest();
    item.setProductId(70L);
    item.setQuantityPackages(quantity);
    item.setDosage("1 tablet every 12h");
    item.setTreatmentTime("7 days");

    CreatePrescriptionRequest request = new CreatePrescriptionRequest();
    request.setRecommendationDate(LocalDate.of(2026, 5, 18));
    request.setItems(List.of(item));
    return request;
  }

  private Prescription prescription() {
    Prescription prescription =
        Prescription.builder()
            .id(90L)
            .visit(visit)
            .vet(vet)
            .clinic(clinic)
            .recommendationDate(LocalDate.of(2026, 5, 18))
            .creationDate(LocalDate.of(2026, 5, 10))
            .build();
    prescription.addItem(
        com.pokiepaws.api.models.PrescriptionItem.builder()
            .id(91L)
            .stockItem(stockItem)
            .quantityPackages(1)
            .dosage("1 tablet every 12h")
            .treatmentTime("7 days")
            .build());
    return prescription;
  }
}
