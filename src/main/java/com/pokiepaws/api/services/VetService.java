package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.animal.AnimalResponse;
import com.pokiepaws.api.dto.vet.CreateVetVisitRequest;
import com.pokiepaws.api.dto.vet.RegisterPatientRequest;
import com.pokiepaws.api.dto.vet.VetListResponse;
import com.pokiepaws.api.dto.vet.VetMeResponse;
import com.pokiepaws.api.dto.vet.VetRequest;
import com.pokiepaws.api.dto.visit.VisitResponse;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.*;
import com.pokiepaws.api.validators.VisitValidator;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VetService {

  private final VetRepository vetRepository;
  private final ClinicRepository clinicRepository;
  private final UserRepository userRepository;
  private final OwnerRepository ownerRepository;
  private final AnimalRepository animalRepository;
  private final VisitRepository visitRepository;
  private final VisitValidator visitValidator;
  private final PasswordEncoder passwordEncoder;

  public List<Vet> getAll() {
    return vetRepository.findAll();
  }

  public VetMeResponse getMe(String email) {
    Vet vet =
        vetRepository
            .findByUserEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Vet not found for email: " + email));
    return VetMeResponse.builder()
        .userId(vet.getUserId())
        .firstName(vet.getFirstName())
        .lastName(vet.getLastName())
        .phone(vet.getPhone())
        .npwz(vet.getNpwz())
        .specialization(vet.getSpecialization())
        .clinicId(vet.getClinic() != null ? vet.getClinic().getId() : null)
        .clinicName(vet.getClinic() != null ? vet.getClinic().getClinicName() : null)
        .build();
  }

  public Vet getById(Long id) {
    return vetRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Vet not found with id: " + id));
  }

  public List<Vet> getByClinic(Long clinicId) {
    return vetRepository.findAllByClinicId(clinicId);
  }

  public List<VetListResponse> getListItemsByClinic(Long clinicId) {
    return vetRepository.findAllByClinicId(clinicId).stream()
        .map(
            vet ->
                VetListResponse.builder()
                    .userId(vet.getUserId())
                    .firstName(vet.getFirstName())
                    .lastName(vet.getLastName())
                    .npwz(vet.getNpwz())
                    .specialization(vet.getSpecialization())
                    .build())
        .toList();
  }

  /**
   * Creates a visit on behalf of the currently authenticated vet. Clinic and vet IDs are derived
   * from the vet's profile — the caller only provides animalId, startsAt, description.
   */
  @Transactional
  public VisitResponse createVisit(String vetEmail, CreateVetVisitRequest req) {
    Vet vet =
        vetRepository
            .findByUserEmail(vetEmail)
            .orElseThrow(() -> ApiException.notFound("Vet not found"));

    if (vet.getClinic() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vet is not assigned to a clinic");
    }

    Animal animal =
        animalRepository
            .findById(req.getAnimalId())
            .orElseThrow(() -> ApiException.notFound("Animal not found"));

    visitValidator.validateRequestedSlot(vet, req.getStartsAt());

    LocalDateTime start = req.getStartsAt();
    LocalDateTime end = start.plusMinutes(visitValidator.slotMinutes());

    Visit visit =
        Visit.builder()
            .animal(animal)
            .clinic(vet.getClinic())
            .vet(vet)
            .startsAt(start)
            .endsAt(end)
            .description(req.getDescription())
            .status(VisitStatus.SCHEDULED)
            .used(false)
            .build();

    Visit saved = visitRepository.save(visit);
    return toVisitResponse(saved);
  }

  /**
   * Registers a new patient (animal) for a given owner email. If no owner with that email exists, a
   * new owner user account is created with a temporary password.
   */
  @Transactional
  public AnimalResponse registerPatient(RegisterPatientRequest req) {
    String ownerEmail = req.getOwnerEmail().trim().toLowerCase();

    Owner owner =
        ownerRepository
            .findByUserEmail(ownerEmail)
            .orElseGet(
                () -> {
                  if (userRepository.findByEmail(ownerEmail).isPresent()) {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "A non-owner account already exists with this email");
                  }
                  String tempPassword = UUID.randomUUID().toString();
                  User user =
                      User.builder()
                          .email(ownerEmail)
                          .password(passwordEncoder.encode(tempPassword))
                          .role(Role.OWNER)
                          .emailVerified(true)
                          .active(true)
                          .build();
                  userRepository.save(user);

                  Owner newOwner =
                      Owner.builder()
                          .user(user)
                          .firstName(req.getOwnerFirstName())
                          .lastName(req.getOwnerLastName())
                          .phoneNumber(req.getOwnerPhone() != null ? req.getOwnerPhone() : "-")
                          .street("-")
                          .houseNumber("-")
                          .postalCode("-")
                          .city("-")
                          .country("PL")
                          .build();
                  return ownerRepository.save(newOwner);
                });

    // validate microchip uniqueness
    if (req.getAnimalMicrochipNumber() != null && !req.getAnimalMicrochipNumber().isBlank()) {
      animalRepository
          .findByMicrochipNumber(req.getAnimalMicrochipNumber())
          .ifPresent(
              existing -> {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Microchip number already registered");
              });
    }

    Animal animal =
        Animal.builder()
            .owner(owner)
            .name(req.getAnimalName())
            .species(req.getAnimalSpecies())
            .breed(req.getAnimalBreed())
            .gender(req.getAnimalGender())
            .color(req.getAnimalColor())
            .microchipNumber(
                req.getAnimalMicrochipNumber() != null && !req.getAnimalMicrochipNumber().isBlank()
                    ? req.getAnimalMicrochipNumber()
                    : null)
            .weight(req.getAnimalWeight())
            .birthDate(req.getAnimalBirthDate())
            .notes(req.getAnimalNotes())
            .active(true)
            .build();

    Animal saved = animalRepository.save(animal);
    return toAnimalResponse(saved);
  }

  @Transactional
  public Vet save(VetRequest request) {
    Clinic clinic =
        clinicRepository
            .findById(request.getClinicId())
            .orElseThrow(() -> new EntityNotFoundException("Clinic not found"));

    Vet vet =
        Vet.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .npwz(request.getNpwz())
            .specialization(request.getSpecialization())
            .clinic(clinic)
            .build();

    return vetRepository.save(vet);
  }

  @Transactional
  public Vet update(Long id, VetRequest request) {
    Vet existingVet = getById(id);

    Clinic clinic =
        clinicRepository
            .findById(request.getClinicId())
            .orElseThrow(() -> new EntityNotFoundException("Clinic not found"));

    existingVet.setFirstName(request.getFirstName());
    existingVet.setLastName(request.getLastName());
    existingVet.setPhone(request.getPhone());
    existingVet.setNpwz(request.getNpwz());
    existingVet.setSpecialization(request.getSpecialization());
    existingVet.setClinic(clinic);

    return vetRepository.save(existingVet);
  }

  @Transactional
  public void delete(Long id) {
    Vet vet = getById(id);
    vetRepository.delete(vet);
    User user = vet.getUser();
    if (user != null) {
      user.setActive(false);
      userRepository.save(user);
      log.info(
          "User account for vet {} {} has been deactivated.",
          vet.getFirstName(),
          vet.getLastName());
    }
  }

  private static VisitResponse toVisitResponse(Visit v) {
    return VisitResponse.builder()
        .id(v.getId())
        .animalId(v.getAnimal().getId())
        .clinicId(v.getClinic().getId())
        .vetUserId(v.getVet().getUserId())
        .startsAt(v.getStartsAt())
        .endsAt(v.getEndsAt())
        .description(v.getDescription())
        .disease(v.getDisease())
        .diagnosis(v.getDiagnosis())
        .recommendations(v.getRecommendations())
        .status(v.getStatus())
        .used(v.isUsed())
        .build();
  }

  private static AnimalResponse toAnimalResponse(Animal a) {
    return AnimalResponse.builder()
        .id(a.getId())
        .name(a.getName())
        .species(a.getSpecies())
        .breed(a.getBreed())
        .gender(a.getGender())
        .color(a.getColor())
        .microchipNumber(a.getMicrochipNumber())
        .weight(a.getWeight())
        .birthDate(a.getBirthDate())
        .notes(a.getNotes())
        .build();
  }
}
