package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.UserAdminRequest;
import com.pokiepaws.api.dto.UserAdminResponse;
import com.pokiepaws.api.models.ActivityLog.LogType;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.WarehouseWorker;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.WarehouseWorkerRepository;
import java.util.List;
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
  private static final String PASSWORD_REQUIRED = "Password is required for new users";
  private static final String NPWZ_REQUIRED = "NPWZ is required for veterinarians";
  private static final String CLINIC_REQUIRED = "Clinic is required for this role";
  private static final String INVALID_ROLE = "Invalid role: ";

  private final UserRepository userRepository;
  private final ClinicRepository clinicRepository;
  private final VetRepository vetRepository;
  private final WarehouseWorkerRepository warehouseWorkerRepository;
  private final PasswordEncoder passwordEncoder;
  private final ActivityLogService activityLogService;

  @Transactional(readOnly = true)
  public List<UserAdminResponse> findAll() {
    return userRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public UserAdminResponse findById(Long id) {
    return toDto(getOrThrow(id));
  }

  @Transactional
  public UserAdminResponse create(UserAdminRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_ALREADY_IN_USE);
    }
    if (request.getPassword() == null || request.getPassword().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_REQUIRED);
    }

    Role role = parseRole(request.getRole());
    validateRoleSpecificFields(role, request);

    User user =
        User.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(role)
            .active(request.isActive())
            .emailVerified(true) // tworzony przez admina, nie wymaga weryfikacji
            .build();
    user = userRepository.save(user);

    Clinic clinic = resolveClinic(request.getClinicId());
    persistRoleProfile(user, role, clinic, request);

    activityLogService.log(
        LogType.data,
        "Dodano użytkownika: "
            + request.getFirstName()
            + " "
            + request.getLastName()
            + " ("
            + user.getEmail()
            + ")",
        clinic != null ? clinic.getName() : null);

    return toDto(user);
  }

  @Transactional
  public UserAdminResponse update(Long id, UserAdminRequest request) {
    User user = getOrThrow(id);
    Role role = parseRole(request.getRole());
    validateRoleSpecificFields(role, request);

    user.setEmail(request.getEmail());
    user.setRole(role);
    user.setActive(request.isActive());
    if (request.getPassword() != null && !request.getPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    userRepository.save(user);

    Clinic clinic = resolveClinic(request.getClinicId());

    // Czyścimy stare profile pod inne role i zapisujemy aktualny
    cleanupOtherRoleProfiles(user, role);
    persistRoleProfile(user, role, clinic, request);

    activityLogService.log(
        LogType.data,
        "Zaktualizowano użytkownika: " + request.getFirstName() + " " + request.getLastName(),
        clinic != null ? clinic.getName() : null);

    return toDto(user);
  }

  @Transactional
  public void delete(Long id) {
    User user = getOrThrow(id);
    String email = user.getEmail();

    cleanupOtherRoleProfiles(user, null);
    userRepository.delete(user);

    activityLogService.log(LogType.data, "Usunięto użytkownika: " + email, null);
  }

  // ─── Helpers ──────────────────────────────────────────────────────────

  private void persistRoleProfile(User user, Role role, Clinic clinic, UserAdminRequest req) {
    switch (role) {
      case VET -> {
        Vet vet =
            vetRepository
                .findById(user.getId())
                .orElseGet(() -> Vet.builder().user(user).build());
        vet.setFirstName(req.getFirstName());
        vet.setLastName(req.getLastName());
        vet.setPhone(req.getPhone());
        vet.setNpwz(req.getNpwz());
        vet.setSpecialization(req.getSpecialization());
        vet.setClinic(clinic);
        vetRepository.save(vet);
      }
      case WAREHOUSE -> {
        WarehouseWorker worker =
            warehouseWorkerRepository
                .findById(user.getId())
                .orElseGet(() -> WarehouseWorker.builder().user(user).build());
        worker.setFirstName(req.getFirstName());
        worker.setLastName(req.getLastName());
        worker.setPhone(req.getPhone());
        worker.setClinic(clinic);
        warehouseWorkerRepository.save(worker);
      }
      default -> {
        // ADMIN / SUPER_ADMIN — brak osobnego profilu, dane tylko w User
      }
    }
  }

  private void cleanupOtherRoleProfiles(User user, Role keep) {
    if (keep != Role.VET) {
      vetRepository.findById(user.getId()).ifPresent(vetRepository::delete);
    }
    if (keep != Role.WAREHOUSE) {
      warehouseWorkerRepository
          .findById(user.getId())
          .ifPresent(warehouseWorkerRepository::delete);
    }
  }

  private void validateRoleSpecificFields(Role role, UserAdminRequest req) {
    if (role == Role.VET && (req.getNpwz() == null || req.getNpwz().isBlank())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, NPWZ_REQUIRED);
    }
    if ((role == Role.VET || role == Role.WAREHOUSE) && req.getClinicId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, CLINIC_REQUIRED);
    }
  }

  private Clinic resolveClinic(Long clinicId) {
    if (clinicId == null) return null;
    return clinicRepository
        .findById(clinicId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clinic not found"));
  }

  private Role parseRole(String role) {
    try {
      return Role.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ROLE + role);
    }
  }

  private User getOrThrow(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
  }

  private UserAdminResponse toDto(User user) {
    String firstName = null;
    String lastName = null;
    String npwz = null;
    String phone = null;
    String specialization = null;
    Clinic clinic = null;

    if (user.getRole() == Role.VET) {
      Vet vet = vetRepository.findById(user.getId()).orElse(null);
      if (vet != null) {
        firstName = vet.getFirstName();
        lastName = vet.getLastName();
        npwz = vet.getNpwz();
        phone = vet.getPhone();
        specialization = vet.getSpecialization();
        clinic = vet.getClinic();
      }
    } else if (user.getRole() == Role.WAREHOUSE) {
      WarehouseWorker worker = warehouseWorkerRepository.findById(user.getId()).orElse(null);
      if (worker != null) {
        firstName = worker.getFirstName();
        lastName = worker.getLastName();
        phone = worker.getPhone();
        clinic = worker.getClinic();
      }
    }

    return UserAdminResponse.builder()
        .id(user.getId())
        .firstName(firstName)
        .lastName(lastName)
        .email(user.getEmail())
        .role(user.getRole().name())
        .clinicId(clinic != null ? clinic.getId() : null)
        .clinicName(clinic != null ? clinic.getName() : null)
        .active(user.isActive())
        .emailVerified(user.isEmailVerified())
        .npwz(npwz)
        .phone(phone)
        .specialization(specialization)
        .build();
  }
}
