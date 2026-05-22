package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.RefreshToken;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.AnimalRepository;
import com.pokiepaws.api.repositories.OwnerDeviceTokenRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.RefreshTokenRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import com.pokiepaws.api.services.OwnerProfileSettingsService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OwnerProfileSettingsServiceTest {

  private static final String OWNER_EMAIL = "owner@pokiepaws.pl";

  @Mock OwnerRepository ownerRepository;
  @Mock UserRepository userRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock RefreshTokenRepository refreshTokenRepository;
  @Mock OwnerDeviceTokenRepository ownerDeviceTokenRepository;
  @Mock AnimalRepository animalRepository;
  @Mock VisitRepository visitRepository;

  private OwnerProfileSettingsService service;

  @BeforeEach
  void setUp() {
    service =
        new OwnerProfileSettingsService(
            ownerRepository,
            userRepository,
            passwordEncoder,
            refreshTokenRepository,
            ownerDeviceTokenRepository,
            animalRepository,
            visitRepository);

    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(OWNER_EMAIL, "password", List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void deleteCurrentOwnerAccount_shouldAnonymizeOwnerDeactivateAccessAndCancelOpenVisits() {
    User user =
        User.builder()
            .id(10L)
            .email(OWNER_EMAIL)
            .password("OLD_HASH")
            .role(Role.OWNER)
            .emailVerified(true)
            .active(true)
            .failedLoginAttempts(3)
            .lockedUntil(LocalDateTime.now().plusMinutes(10))
            .build();
    Owner owner =
        Owner.builder()
            .userId(10L)
            .user(user)
            .firstName("Gabriela")
            .lastName("Grabarska")
            .phoneNumber("123456789")
            .street("Zielona")
            .houseNumber("1")
            .postalCode("59-220")
            .city("Legnica")
            .country("Poland")
            .build();
    Animal animal = Animal.builder().owner(owner).active(true).build();
    Visit scheduledVisit = Visit.builder().status(VisitStatus.SCHEDULED).build();
    Visit confirmedVisit = Visit.builder().status(VisitStatus.CONFIRMED).build();
    RefreshToken refreshToken =
        RefreshToken.builder().user(user).tokenHash("TOKEN_HASH").revoked(false).build();

    when(ownerRepository.findByUserEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
    when(animalRepository.findAllByOwnerAndActiveTrue(owner)).thenReturn(List.of(animal));
    when(visitRepository.findAllByAnimalOwnerUserIdAndStatusIn(
            10L, List.of(VisitStatus.SCHEDULED, VisitStatus.CONFIRMED, VisitStatus.IN_PROGRESS)))
        .thenReturn(List.of(scheduledVisit, confirmedVisit));
    when(refreshTokenRepository.findAllByUserAndRevokedFalse(user))
        .thenReturn(List.of(refreshToken));
    when(passwordEncoder.encode(anyString())).thenReturn("NEW_HASH");

    service.deleteCurrentOwnerAccount();

    assertThat(animal.isActive()).isFalse();
    assertThat(scheduledVisit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
    assertThat(confirmedVisit.getStatus()).isEqualTo(VisitStatus.CANCELLED);
    assertThat(refreshToken.isRevoked()).isTrue();
    assertThat(owner.getFirstName()).isEqualTo("Deleted");
    assertThat(owner.getLastName()).isEqualTo("Owner");
    assertThat(user.getEmail()).isEqualTo("deleted-owner-10@pokiepaws.local");
    assertThat(user.getPassword()).isEqualTo("NEW_HASH");
    assertThat(user.isEmailVerified()).isFalse();
    assertThat(user.isActive()).isFalse();
    assertThat(user.getFailedLoginAttempts()).isZero();
    assertThat(user.getLockedUntil()).isNull();

    verify(ownerDeviceTokenRepository).deleteAllByOwnerUserId(10L);
    verify(ownerRepository).save(owner);
    verify(userRepository).save(user);
  }
}
