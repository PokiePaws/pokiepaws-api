package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.admin.UserAdminRequest;
import com.pokiepaws.api.dto.admin.UserAdminResponse;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.WarehouseWorkerRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserAdminService {

  private static final String USER_NOT_FOUND = "User not found";
  private static final String EMAIL_ALREADY_IN_USE = "Email already in use";
  private static final String PASSWORD_REQUIRED = "Password is required";
  private static final String INVALID_ROLE = "Invalid role";
  private static final String VET_REQUIRES_CLINIC = "Vet requires clinic";
  private static final String VET_REQUIRES_NPWZ = "Vet requires NPWZ";
  private static final String NPWZ_ALREADY_IN_USE = "NPWZ already in use";

  private final UserRepository userRepository;
  private final OwnerRepository ownerRepository;
  private final VetRepository vetRepository;
  private final ClinicRepository clinicRepository;
  private final WarehouseWorkerRepository warehouseWorkerRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public List<UserAdminResponse> findAll() {
    return userRepository.findAll().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public UserAdminResponse findById(Long id) {
    return toResponse(getUser(id));
  }

  @Transactional
  public UserAdminResponse create(UserAdminRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_ALREADY_IN_USE);
    }
    if (isBlank(request.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_REQUIRED);
    }

    Role role = parseRole(request.getRole());
    User user =
        userRepository.save(
            User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .emailVerified(true)
                .active(request.isActive())
                .build());

    syncProfile(user, request);
    return toResponse(user);
  }

  @Transactional
  public UserAdminResponse update(Long id, UserAdminRequest request) {
    User user = getUser(id);
    userRepository
        .findByEmail(request.getEmail())
        .filter(existing -> !existing.getId().equals(id))
        .ifPresent(
            existing -> {
              throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_ALREADY_IN_USE);
            });

    user.setEmail(request.getEmail());
    user.setRole(parseRole(request.getRole()));
    user.setActive(request.isActive());
    if (!isBlank(request.getPassword())) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }

    syncProfile(user, request);
    return toResponse(userRepository.save(user));
  }

  @Transactional
  public void delete(Long id) {
    User user = getUser(id);
    user.setActive(false);
    userRepository.save(user);

    vetRepository
        .findById(id)
        .ifPresent(
            vet -> {
              vet.setActive(false);
              vetRepository.save(vet);
            });
    warehouseWorkerRepository
        .findById(id)
        .ifPresent(
            worker -> {
              worker.setActive(false);
              warehouseWorkerRepository.save(worker);
            });
  }

  private void syncProfile(User user, UserAdminRequest request) {
    Role role = user.getRole();
    if (role != Role.VET) {
      vetRepository
          .findById(user.getId())
          .ifPresent(
              vet -> {
                vet.setActive(false);
                vetRepository.save(vet);
              });
    }
    if (role != Role.OWNER) {
      ownerRepository.findById(user.getId()).ifPresent(ownerRepository::delete);
    }
    if (role != Role.WAREHOUSE) {
      warehouseWorkerRepository
          .findById(user.getId())
          .ifPresent(
              worker -> {
                worker.setActive(false);
                warehouseWorkerRepository.save(worker);
              });
    }

    if (role == Role.VET) {
      syncVetProfile(user, request);
    } else if (role == Role.OWNER) {
      syncOwnerProfile(user, request);
    }
  }

  private void syncVetProfile(User user, UserAdminRequest request) {
    if (request.getClinicId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, VET_REQUIRES_CLINIC);
    }
    if (isBlank(request.getNpwz())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, VET_REQUIRES_NPWZ);
    }

    vetRepository
        .findByNpwz(request.getNpwz())
        .filter(existing -> !existing.getUserId().equals(user.getId()))
        .ifPresent(
            existing -> {
              throw new ResponseStatusException(HttpStatus.CONFLICT, NPWZ_ALREADY_IN_USE);
            });

    Clinic clinic =
        clinicRepository
            .findById(request.getClinicId())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

    Vet vet =
        vetRepository.findById(user.getId()).orElseGet(() -> Vet.builder().user(user).build());
    vet.setFirstName(request.getFirstName());
    vet.setLastName(request.getLastName());
    vet.setPhone(request.getPhone());
    vet.setNpwz(request.getNpwz());
    vet.setSpecialization(request.getSpecialization());
    vet.setClinic(clinic);
    vet.setActive(request.isActive());
    vetRepository.save(vet);
  }

  private void syncOwnerProfile(User user, UserAdminRequest request) {
    Owner owner =
        ownerRepository.findById(user.getId()).orElseGet(() -> Owner.builder().user(user).build());
    owner.setFirstName(request.getFirstName());
    owner.setLastName(request.getLastName());
    owner.setPhoneNumber(defaultIfBlank(request.getPhone(), ""));
    owner.setStreet(defaultIfBlank(owner.getStreet(), "-"));
    owner.setHouseNumber(defaultIfBlank(owner.getHouseNumber(), "-"));
    owner.setPostalCode(defaultIfBlank(owner.getPostalCode(), "00-000"));
    owner.setCity(defaultIfBlank(owner.getCity(), "-"));
    owner.setCountry(defaultIfBlank(owner.getCountry(), "-"));
    ownerRepository.save(owner);
  }

  private UserAdminResponse toResponse(User user) {
    UserAdminResponse.UserAdminResponseBuilder response =
        UserAdminResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .active(user.isActive())
            .emailVerified(user.isEmailVerified());

    vetRepository
        .findById(user.getId())
        .ifPresent(
            vet ->
                response
                    .firstName(vet.getFirstName())
                    .lastName(vet.getLastName())
                    .clinicId(vet.getClinic() != null ? vet.getClinic().getId() : null)
                    .clinicName(vet.getClinic() != null ? vet.getClinic().getClinicName() : null)
                    .npwz(vet.getNpwz())
                    .phone(vet.getPhone())
                    .specialization(vet.getSpecialization()));

    ownerRepository
        .findById(user.getId())
        .ifPresent(
            owner ->
                response
                    .firstName(owner.getFirstName())
                    .lastName(owner.getLastName())
                    .phone(owner.getPhoneNumber()));

    warehouseWorkerRepository
        .findByUserId(user.getId())
        .ifPresent(
            worker ->
                response
                    .firstName(worker.getFirstName())
                    .lastName(worker.getLastName())
                    .phone(worker.getPhoneNumber()));

    return response.build();
  }

  private User getUser(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
  }

  private Role parseRole(String role) {
    try {
      return Role.valueOf(role.toUpperCase(Locale.ROOT));
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ROLE);
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String defaultIfBlank(String value, String defaultValue) {
    return isBlank(value) ? defaultValue : value;
  }
}
