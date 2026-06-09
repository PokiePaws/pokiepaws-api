package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.vet.VetListResponse;
import com.pokiepaws.api.dto.vet.VetMeResponse;
import com.pokiepaws.api.dto.vet.VetRequest;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VetService {

  private final VetRepository vetRepository;
  private final ClinicRepository clinicRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public VetMeResponse getMe() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Vet vet =
        vetRepository
            .findByUserEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("Vet profile not found"));
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

  public List<Vet> getAll() {
    return vetRepository.findAll();
  }

  public Vet getById(Long id) {
    return vetRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Vet not found with id: " + id));
  }

  public Vet getByEmail(String email) {
    return vetRepository
        .findByUserEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Vet not found with email: " + email));
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
}
