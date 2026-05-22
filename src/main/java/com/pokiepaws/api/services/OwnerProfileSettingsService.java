package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.ownersettings.OwnerProfileSettingsResponse;
import com.pokiepaws.api.dto.ownersettings.SettingsUpdateOwnerAddressRequest;
import com.pokiepaws.api.dto.ownersettings.SettingsUpdateOwnerPasswordRequest;
import com.pokiepaws.api.dto.ownersettings.SettingsUpdateOwnerPhoneRequest;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.OwnerDeviceTokenRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.RefreshTokenRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerProfileSettingsService {

  private static final String CURRENT_PASSWORD_INVALID = "Current password is invalid";
  private static final String PASSWORD_MIN_LENGTH =
      "The password must be at least 12 characters long";
  private static final String PASSWORD_NEEDS_UPPERCASE =
      "The password must contain at least one uppercase letter";
  private static final String PASSWORD_NEEDS_SPECIAL =
      "The password must contain a special character";
  private static final String PASSWORD_MUST_NOT_CONTAIN_EMAIL =
      "The password must not contain an email address";

  private final OwnerRepository ownerRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RefreshTokenRepository refreshTokenRepository;
  private final OwnerDeviceTokenRepository ownerDeviceTokenRepository;
  private final AnimalRepository animalRepository;
  private final VisitRepository visitRepository;

  @Transactional(readOnly = true)
  public OwnerProfileSettingsResponse getCurrentOwnerProfile() {
    return toResponse(getCurrentOwner());
  }

  @Transactional
  public void updatePhone(SettingsUpdateOwnerPhoneRequest request) {
    Owner owner = getCurrentOwner();
    owner.setPhoneNumber(request.getPhoneNumber());
    ownerRepository.save(owner);
  }

  @Transactional
  public void updateAddress(SettingsUpdateOwnerAddressRequest request) {
    Owner owner = getCurrentOwner();
    owner.setStreet(request.getStreet());
    owner.setHouseNumber(request.getHouseNumber());
    owner.setApartmentNumber(blankToNull(request.getApartmentNumber()));
    owner.setPostalCode(request.getPostalCode());
    owner.setCity(request.getCity());
    owner.setCountry(request.getCountry());
    ownerRepository.save(owner);
  }

  @Transactional
  public void updatePassword(SettingsUpdateOwnerPasswordRequest request) {
    Owner owner = getCurrentOwner();
    User user = owner.getUser();
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw ApiException.badRequest(CURRENT_PASSWORD_INVALID);
    }
    validatePasswordPolicy(request.getNewPassword(), user.getEmail());
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  private Owner getCurrentOwner() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return ownerRepository
        .findByUserEmail(email)
        .orElseThrow(() -> ApiException.forbidden(ApiErrorMessage.OWNER_PROFILE_NOT_FOUND));
  }

  private OwnerProfileSettingsResponse toResponse(Owner owner) {
    User user = owner.getUser();
    return OwnerProfileSettingsResponse.builder()
        .userId(owner.getUserId())
        .email(user.getEmail())
        .firstName(owner.getFirstName())
        .lastName(owner.getLastName())
        .phoneNumber(owner.getPhoneNumber())
        .street(owner.getStreet())
        .houseNumber(owner.getHouseNumber())
        .apartmentNumber(owner.getApartmentNumber())
        .postalCode(owner.getPostalCode())
        .city(owner.getCity())
        .country(owner.getCountry())
        .build();
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private void validatePasswordPolicy(String password, String email) {
    if (password.length() < 12) {
      throw ApiException.badRequest(PASSWORD_MIN_LENGTH);
    }
    if (!password.matches(".*[A-Z].*")) {
      throw ApiException.badRequest(PASSWORD_NEEDS_UPPERCASE);
    }
    if (!password.matches(".*[!@#$%^&*()].*")) {
      throw ApiException.badRequest(PASSWORD_NEEDS_SPECIAL);
    }

    if (email != null) {
      String passwordLower = password.toLowerCase(Locale.ROOT);
      String emailLower = email.toLowerCase(Locale.ROOT);
      if (passwordLower.contains(emailLower)) {
        throw ApiException.badRequest(PASSWORD_MUST_NOT_CONTAIN_EMAIL);
      }
    }
  }

  @Transactional
  public void deleteCurrentOwnerAccount() {
    Owner owner = getCurrentOwner();
    User user = owner.getUser();
    Long userId = owner.getUserId();

    animalRepository.findAllByOwnerAndActiveTrue(owner).forEach(animal -> animal.setActive(false));

    visitRepository
        .findAllByAnimalOwnerUserIdAndStatusIn(
            userId, List.of(VisitStatus.SCHEDULED, VisitStatus.CONFIRMED, VisitStatus.IN_PROGRESS))
        .forEach(visit -> visit.setStatus(VisitStatus.CANCELLED));

    ownerDeviceTokenRepository.deleteAllByOwnerUserId(userId);

    refreshTokenRepository
        .findAllByUserAndRevokedFalse(user)
        .forEach(refreshToken -> refreshToken.setRevoked(true));

    owner.setFirstName("Deleted");
    owner.setLastName("Owner");
    owner.setPhoneNumber("000000000");
    owner.setStreet("-");
    owner.setHouseNumber("-");
    owner.setApartmentNumber(null);
    owner.setPostalCode("00-000");
    owner.setCity("-");
    owner.setCountry("-");

    user.setEmail("deleted-owner-" + userId + "@pokiepaws.local");
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
    user.setEmailVerified(false);
    user.setActive(false);
    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);

    ownerRepository.save(owner);
    userRepository.save(user);
  }
}
