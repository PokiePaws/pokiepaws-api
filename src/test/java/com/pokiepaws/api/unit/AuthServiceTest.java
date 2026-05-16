package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.pokiepaws.api.dto.auth.AuthRequest;
import com.pokiepaws.api.dto.auth.AuthResponse;
import com.pokiepaws.api.dto.auth.RegisterRequest;
import com.pokiepaws.api.dto.auth.ResetPasswordRequest;
import com.pokiepaws.api.models.EmailVerificationToken;
import com.pokiepaws.api.models.ForgotPasswordToken;
import com.pokiepaws.api.models.MfaToken;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.RefreshToken;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.EmailVerificationTokenRepository;
import com.pokiepaws.api.repositories.ForgotPasswordTokenRepository;
import com.pokiepaws.api.repositories.MfaTokenRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.RefreshTokenRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.security.JwtService;
import com.pokiepaws.api.services.ActivityLogService;
import com.pokiepaws.api.services.AuthService;
import com.pokiepaws.api.services.EmailService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private Clock clock;

  @Mock UserRepository userRepository;
  @Mock OwnerRepository ownerRepository;

  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;
  @Mock AuthenticationManager authenticationManager;

  @Mock EmailVerificationTokenRepository tokenRepository;
  @Mock EmailService emailService;
  @Mock ForgotPasswordTokenRepository forgotPasswordTokenRepository;
  @Mock RefreshTokenRepository refreshTokenRepository;
  @Mock MfaTokenRepository mfaTokenRepository;
  @Mock ActivityLogService activityLogService;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    clock = Clock.fixed(Instant.parse("2026-04-18T10:00:00Z"), ZoneId.of("UTC"));

    authService =
        new AuthService(
            clock,
            userRepository,
            ownerRepository,
            passwordEncoder,
            jwtService,
            authenticationManager,
            tokenRepository,
            emailService,
            forgotPasswordTokenRepository,
            refreshTokenRepository,
            mfaTokenRepository,
            activityLogService);

    ReflectionTestUtils.setField(authService, "baseUrl", "${app.base-url}");
    ReflectionTestUtils.setField(authService, "frontendUrl", "${app.frontend-url}");
    ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
  }

  private RegisterRequest validRegister() {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Anna");
    request.setLastName("Kowalska");
    request.setPhoneNumber("+48123456789");
    request.setStreet("Sejmowa");
    request.setHouseNumber("2A");
    request.setApartmentNumber(null);
    request.setPostalCode("59-220");
    request.setCity("Legnica");
    request.setCountry("Poland");
    request.setEmail("newowner@pokiepaws.pl");
    request.setPassword("Owner1234!");
    return request;
  }

  private AuthRequest authRequest(String email) {
    AuthRequest request = new AuthRequest();
    request.setEmail(email);
    request.setPassword("Owner1234!");
    return request;
  }

  private ResetPasswordRequest resetRequest(String token, String newPassword) {
    ResetPasswordRequest request = new ResetPasswordRequest();
    ReflectionTestUtils.setField(request, "token", token);
    ReflectionTestUtils.setField(request, "newPassword", newPassword);
    return request;
  }

  private ForgotPasswordToken validForgotToken(User user, String tokenHash) {
    return ForgotPasswordToken.builder()
        .user(user)
        .tokenHash(tokenHash)
        .used(false)
        .expiresAt(LocalDateTime.now(clock).plusMinutes(10))
        .build();
  }

  @Test
  void register_shouldThrow409_whenEmailAlreadyInUse() {
    RegisterRequest request = validRegister();
    when(userRepository.existsByEmail("newowner@pokiepaws.pl")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
              assertThat(rse.getReason()).isEqualTo("Email already in use");
            });

    verify(userRepository, never()).save(any(User.class));
    verify(ownerRepository, never()).save(any(Owner.class));
    verify(tokenRepository, never()).save(any(EmailVerificationToken.class));
    verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyString());
  }

  @Test
  void register_shouldSaveUserOwnerToken_andSendVerificationEmail() {
    RegisterRequest request = validRegister();

    when(userRepository.existsByEmail("newowner@pokiepaws.pl")).thenReturn(false);
    when(passwordEncoder.encode("Owner1234!")).thenReturn("HASH");

    when(userRepository.save(any(User.class)))
        .thenAnswer(
            invocation -> {
              User user = invocation.getArgument(0);
              user.setId(1L);
              return user;
            });

    when(ownerRepository.save(any(Owner.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(tokenRepository.save(any(EmailVerificationToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    authService.register(request);

    ArgumentCaptor<User> savedUserCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(savedUserCaptor.capture());
    User savedUser = savedUserCaptor.getValue();
    assertThat(savedUser.getEmail()).isEqualTo("newowner@pokiepaws.pl");
    assertThat(savedUser.getPassword()).isEqualTo("HASH");
    assertThat(savedUser.getRole()).isEqualTo(Role.OWNER);
    assertThat(savedUser.isActive()).isTrue();
    assertThat(savedUser.isEmailVerified()).isFalse();

    ArgumentCaptor<Owner> ownerArgumentCaptor = ArgumentCaptor.forClass(Owner.class);
    verify(ownerRepository).save(ownerArgumentCaptor.capture());
    Owner savedOwner = ownerArgumentCaptor.getValue();
    assertThat(savedOwner.getUser()).isNotNull();
    assertThat(savedOwner.getUser().getEmail()).isEqualTo("newowner@pokiepaws.pl");
    assertThat(savedOwner.getFirstName()).isEqualTo("Anna");
    assertThat(savedOwner.getPostalCode()).isEqualTo("59-220");

    ArgumentCaptor<EmailVerificationToken> tokenArgumentCaptor =
        ArgumentCaptor.forClass(EmailVerificationToken.class);
    verify(tokenRepository).save(tokenArgumentCaptor.capture());
    EmailVerificationToken savedToken = tokenArgumentCaptor.getValue();
    assertThat(savedToken.getToken()).isNotBlank();
    assertThat(savedToken.isUsed()).isFalse();

    LocalDateTime expected = LocalDateTime.now(clock).plusHours(24);
    assertThat(savedToken.getExpiresAt()).isEqualTo(expected);

    verify(emailService)
        .sendVerificationEmail(eq("newowner@pokiepaws.pl"), anyString(), eq("${app.base-url}"));
  }

  @Test
  void login_shouldThrow401_whenUserMissing() {
    AuthRequest request = authRequest("ownernotfound@pokiepaws.pl");

    when(userRepository.findByEmail("ownernotfound@pokiepaws.pl")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
              assertThat(rse.getReason()).isEqualTo("Invalid email or password");
            });

    verify(authenticationManager, never()).authenticate(any());
    verify(jwtService, never()).generateToken(any(UserDetails.class));
    verify(activityLogService)
        .logFor(
            eq("ownernotfound@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("Failed login: unknown account"),
            isNull());
  }

  @Test
  void login_shouldThrow403_whenEmailNotVerified() {
    AuthRequest request = authRequest("notverified@pokiepaws.pl");

    User user =
        User.builder()
            .email("notverified@pokiepaws.pl")
            .password("HASH")
            .emailVerified(false)
            .active(true)
            .role(Role.OWNER)
            .build();

    when(userRepository.findByEmail("notverified@pokiepaws.pl")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
              assertThat(rse.getReason()).isEqualTo("Please verify your email address first.");
            });

    verify(authenticationManager, never()).authenticate(any());
  }

  @Test
  void login_shouldAuthenticateAndReturnTokens_whenVerifiedOwner() {
    User user =
        User.builder()
            .email("verified@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .build();

    when(userRepository.findByEmail("verified@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mock(Authentication.class));
    when(jwtService.generateToken(any(UserDetails.class))).thenReturn("ACCESS");
    when(passwordEncoder.encode(anyString())).thenReturn("REFRESH_HASH");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthResponse response = authService.login(authRequest("verified@pokiepaws.pl"));

    assertThat(response.getAccessToken()).isEqualTo("ACCESS");
    assertThat(response.getRefreshToken()).isNotBlank();
    assertThat(response.getEmail()).isEqualTo("verified@pokiepaws.pl");
    assertThat(response.getRole()).isEqualTo("OWNER");
    assertThat(response.isMfaRequired()).isFalse();

    verify(authenticationManager)
        .authenticate(
            argThat(
                token ->
                    token instanceof UsernamePasswordAuthenticationToken
                        && "verified@pokiepaws.pl".equals(token.getPrincipal())
                        && "Owner1234!".equals(token.getCredentials())));
    verify(jwtService).generateToken(any(UserDetails.class));
    verify(refreshTokenRepository).save(any(RefreshToken.class));
    verify(activityLogService)
        .logFor(
            eq("verified@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("Login successful"),
            isNull());
  }

  @Test
  void login_shouldIncrementFailedAttemptsAndThrow401_whenPasswordInvalid() {
    User user =
        User.builder()
            .email("verified@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .build();

    when(userRepository.findByEmail("verified@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("bad"));

    assertThatThrownBy(() -> authService.login(authRequest("verified@pokiepaws.pl")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
              assertThat(rse.getReason()).isEqualTo("Invalid email or password");
            });

    assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
    assertThat(user.getLockedUntil()).isNull();
    verify(userRepository).save(user);
    verify(activityLogService)
        .logFor(
            eq("verified@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("Failed login"),
            isNull());
  }

  @Test
  void login_shouldLockAccountFor15Minutes_afterFifthFailedAttempt() {
    User user =
        User.builder()
            .email("verified@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .failedLoginAttempts(4)
            .build();

    when(userRepository.findByEmail("verified@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("bad"));

    assertThatThrownBy(() -> authService.login(authRequest("verified@pokiepaws.pl")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            });

    assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
    assertThat(user.getLockedUntil()).isEqualTo(LocalDateTime.now(clock).plusMinutes(15));
    verify(userRepository).save(user);
  }

  @Test
  void login_shouldThrow423_whenAccountIsTemporarilyLocked() {
    User user =
        User.builder()
            .email("verified@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .failedLoginAttempts(5)
            .lockedUntil(LocalDateTime.now(clock).plusMinutes(5))
            .build();

    when(userRepository.findByEmail("verified@pokiepaws.pl")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(authRequest("verified@pokiepaws.pl")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
              assertThat(rse.getReason())
                  .isEqualTo("The account is temporarily locked. Please try again later.");
            });

    verify(authenticationManager, never()).authenticate(any());
    verify(activityLogService)
        .logFor(
            eq("verified@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("Failed login: account temporarily locked"),
            isNull());
  }

  @Test
  void login_shouldResetFailedAttempts_whenPasswordValidAfterLockExpired() {
    User user =
        User.builder()
            .email("verified@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .failedLoginAttempts(5)
            .lockedUntil(LocalDateTime.now(clock).minusMinutes(1))
            .build();

    when(userRepository.findByEmail("verified@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mock(Authentication.class));
    when(jwtService.generateToken(any(UserDetails.class))).thenReturn("ACCESS");
    when(passwordEncoder.encode(anyString())).thenReturn("REFRESH_HASH");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    authService.login(authRequest("verified@pokiepaws.pl"));

    assertThat(user.getFailedLoginAttempts()).isZero();
    assertThat(user.getLockedUntil()).isNull();
    verify(userRepository).save(user);
  }

  @Test
  void login_shouldReturnMfaRequiredAndSendMfaLink_whenVerifiedAdmin() {
    User user =
        User.builder()
            .email("admin@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.ADMIN)
            .build();

    when(userRepository.findByEmail("admin@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mock(Authentication.class));
    when(mfaTokenRepository.findAllByUserAndCreatedAtAfter(eq(user), any(LocalDateTime.class)))
        .thenReturn(List.of());
    when(mfaTokenRepository.findAllByUserAndUsedFalse(user)).thenReturn(List.of());
    when(passwordEncoder.encode(anyString())).thenReturn("MFA_HASH");
    when(mfaTokenRepository.save(any(MfaToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthResponse response = authService.login(authRequest("admin@pokiepaws.pl"));

    assertThat(response.isMfaRequired()).isTrue();
    assertThat(response.getAccessToken()).isNull();
    assertThat(response.getRefreshToken()).isNull();
    assertThat(response.getEmail()).isEqualTo("admin@pokiepaws.pl");
    assertThat(response.getRole()).isEqualTo("ADMIN");

    verify(mfaTokenRepository).save(any(MfaToken.class));
    verify(emailService)
        .sendMfaLink(eq("admin@pokiepaws.pl"), anyString(), eq("${app.frontend-url}"));
    verify(activityLogService)
        .logFor(
            eq("admin@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("2FA challenge sent"),
            isNull());
    verify(jwtService, never()).generateToken(any(UserDetails.class));
    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  void login_shouldThrow429_whenMfaRateLimitExceeded() {
    User user =
        User.builder()
            .email("admin@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.ADMIN)
            .build();

    when(userRepository.findByEmail("admin@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(mock(Authentication.class));
    when(mfaTokenRepository.findAllByUserAndCreatedAtAfter(eq(user), any(LocalDateTime.class)))
        .thenReturn(
            List.of(
                MfaToken.builder().build(),
                MfaToken.builder().build(),
                MfaToken.builder().build()));

    assertThatThrownBy(() -> authService.login(authRequest("admin@pokiepaws.pl")))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
              assertThat(rse.getReason())
                  .isEqualTo("Too many 2FA requests. Please try again later.");
            });

    verify(mfaTokenRepository, never()).save(any());
    verify(emailService, never()).sendMfaLink(anyString(), anyString(), anyString());
  }

  @Test
  void verifyMfa_shouldIssueTokensAndMarkMfaTokenUsed_whenTokenValid() {
    User user =
        User.builder()
            .email("vet@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.VET)
            .build();
    MfaToken mfaToken =
        MfaToken.builder()
            .user(user)
            .tokenHash("MFA_HASH")
            .used(false)
            .expiresAt(LocalDateTime.now(clock).plusMinutes(10))
            .createdAt(LocalDateTime.now(clock))
            .build();

    when(mfaTokenRepository.findAllByUsedFalseAndExpiresAtAfter(any(LocalDateTime.class)))
        .thenReturn(List.of(mfaToken));
    when(passwordEncoder.matches("plain-mfa-token", "MFA_HASH")).thenReturn(true);
    when(jwtService.generateToken(any(UserDetails.class))).thenReturn("ACCESS");
    when(passwordEncoder.encode(anyString())).thenReturn("REFRESH_HASH");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthResponse response = authService.verifyMfa("plain-mfa-token");

    assertThat(response.getAccessToken()).isEqualTo("ACCESS");
    assertThat(response.getRefreshToken()).isNotBlank();
    assertThat(response.getEmail()).isEqualTo("vet@pokiepaws.pl");
    assertThat(response.getRole()).isEqualTo("VET");
    assertThat(response.isMfaRequired()).isFalse();
    assertThat(mfaToken.isUsed()).isTrue();

    verify(mfaTokenRepository).save(mfaToken);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
    verify(activityLogService)
        .logFor(
            eq("vet@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("2FA verification successful"),
            isNull());
  }

  @Test
  void refresh_shouldRotateRefreshToken_whenTokenValid() {
    User user =
        User.builder()
            .email("owner@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .build();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash("OLD_HASH")
            .revoked(false)
            .expiresAt(LocalDateTime.now(clock).plusDays(1))
            .createdAt(LocalDateTime.now(clock))
            .build();

    when(refreshTokenRepository.findAllByRevokedFalseAndExpiresAtAfter(any(LocalDateTime.class)))
        .thenReturn(List.of(refreshToken));
    when(passwordEncoder.matches("old-refresh-token", "OLD_HASH")).thenReturn(true);
    when(jwtService.generateToken(any(UserDetails.class))).thenReturn("NEW_ACCESS");
    when(passwordEncoder.encode(anyString())).thenReturn("NEW_REFRESH_HASH");
    when(refreshTokenRepository.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AuthResponse response = authService.refresh("old-refresh-token");

    assertThat(refreshToken.isRevoked()).isTrue();
    assertThat(response.getAccessToken()).isEqualTo("NEW_ACCESS");
    assertThat(response.getRefreshToken()).isNotBlank();
    assertThat(response.getEmail()).isEqualTo("owner@pokiepaws.pl");

    verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    verify(activityLogService)
        .logFor(
            eq("owner@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("Refresh token rotated"),
            isNull());
  }

  @Test
  void logout_shouldRevokeRefreshTokenAndWriteAuditLog_whenTokenMatches() {
    User user =
        User.builder()
            .email("owner@pokiepaws.pl")
            .password("HASH")
            .emailVerified(true)
            .active(true)
            .role(Role.OWNER)
            .build();
    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash("REFRESH_HASH")
            .revoked(false)
            .expiresAt(LocalDateTime.now(clock).plusDays(1))
            .createdAt(LocalDateTime.now(clock))
            .build();

    when(refreshTokenRepository.findAllByRevokedFalseAndExpiresAtAfter(any(LocalDateTime.class)))
        .thenReturn(List.of(refreshToken));
    when(passwordEncoder.matches("refresh-token", "REFRESH_HASH")).thenReturn(true);

    authService.logout("refresh-token");

    assertThat(refreshToken.isRevoked()).isTrue();
    verify(refreshTokenRepository).save(refreshToken);
    verify(activityLogService)
        .logFor(
            eq("owner@pokiepaws.pl"),
            eq(com.pokiepaws.api.models.ActivityLog.LogType.login),
            eq("Logout"),
            isNull());
  }

  @Test
  void verifyEmail_shouldThrow400_whenTokenInvalid() {
    when(tokenRepository.findByToken("bad")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.verifyEmail("bad"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("Invalid token");
            });
  }

  @Test
  void verifyEmail_shouldThrow400_whenTokenAlreadyUsed() {
    EmailVerificationToken token =
        EmailVerificationToken.builder()
            .token("token_used")
            .user(User.builder().email("tokenused@pokiepaws.pl").build())
            .expiresAt(LocalDateTime.now(clock).plusMinutes(10))
            .used(true)
            .build();

    when(tokenRepository.findByToken("token_used")).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> authService.verifyEmail("token_used"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("The token has already been used");
            });

    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).save(any());
  }

  @Test
  void verifyEmail_shouldThrow400_whenTokenExpired() {
    EmailVerificationToken token =
        EmailVerificationToken.builder()
            .token("token_expired")
            .user(User.builder().email("tokenexpired@pokiepaws.pl").build())
            .expiresAt(LocalDateTime.now(clock).minusSeconds(1))
            .used(false)
            .build();

    when(tokenRepository.findByToken("token_expired")).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> authService.verifyEmail("token_expired"))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("The token has expired");
            });

    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).save(any());
  }

  @Test
  void verifyEmail_shouldMarkUserVerified_andTokenUsed_whenValid() {
    User user =
        User.builder()
            .id(10L)
            .email("valid@pokiepaws.pl")
            .password("HASH")
            .role(Role.OWNER)
            .active(true)
            .emailVerified(false)
            .build();

    EmailVerificationToken token =
        EmailVerificationToken.builder()
            .token("token_valid")
            .user(user)
            .expiresAt(LocalDateTime.now(clock).plusMinutes(10))
            .used(false)
            .build();

    when(tokenRepository.findByToken("token_valid")).thenReturn(Optional.of(token));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(tokenRepository.save(any(EmailVerificationToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    String message = authService.verifyEmail("token_valid");
    assertThat(message).contains("confirmed");

    assertThat(user.isEmailVerified()).isTrue();
    assertThat(token.isUsed()).isTrue();

    verify(userRepository).save(argThat(User::isEmailVerified));
    verify(tokenRepository).save(argThat(EmailVerificationToken::isUsed));
  }

  @Test
  void forgotPassword_shouldDeleteOldTokensAndSendEmail_whenUserExists() {
    User user = User.builder().email("forgotpassword@pokiepaws.pl").build();
    when(userRepository.findByEmail("forgotpassword@pokiepaws.pl")).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(anyString())).thenReturn("NEW_HASH");

    authService.forgotPassword("forgotpassword@pokiepaws.pl");

    verify(forgotPasswordTokenRepository).deleteAllByUser(user);

    ArgumentCaptor<ForgotPasswordToken> tokenArgumentCaptor =
        ArgumentCaptor.forClass(ForgotPasswordToken.class);
    verify(forgotPasswordTokenRepository).save(tokenArgumentCaptor.capture());

    ForgotPasswordToken savedtoken = tokenArgumentCaptor.getValue();
    assertThat(savedtoken.getUser()).isEqualTo(user);
    assertThat(savedtoken.getTokenHash()).isEqualTo("NEW_HASH");
    assertThat(savedtoken.isUsed()).isFalse();

    LocalDateTime expected = LocalDateTime.now(clock).plusMinutes(15);
    assertThat(savedtoken.getExpiresAt()).isEqualTo(expected);

    ArgumentCaptor<String> plainTokenCap = ArgumentCaptor.forClass(String.class);
    verify(emailService)
        .sendForgotPasswordEmail(
            eq("forgotpassword@pokiepaws.pl"), plainTokenCap.capture(), eq("${app.base-url}"));

    assertThat(plainTokenCap.getValue()).isNotBlank();
    assertThat(plainTokenCap.getValue()).isNotEqualTo("NEW_HASH");
  }

  @Test
  void forgotPassword_shouldDoNothing_whenUserNotFound() {
    when(userRepository.findByEmail("notfound@pokiepaws.pl")).thenReturn(Optional.empty());

    authService.forgotPassword("notfound@pokiepaws.pl");

    verify(forgotPasswordTokenRepository, never()).deleteAllByUser(any());
    verify(forgotPasswordTokenRepository, never()).save(any());
    verify(emailService, never()).sendForgotPasswordEmail(anyString(), anyString(), anyString());
  }

  @Test
  void validateResetToken_shouldReturnTrue_whenAnyTokenMatches() {
    ForgotPasswordToken tokenvalid = ForgotPasswordToken.builder().tokenHash("HASH").build();
    when(forgotPasswordTokenRepository.findAllByUsedFalseAndExpiresAtAfter(
            any(LocalDateTime.class)))
        .thenReturn(List.of(tokenvalid));
    when(passwordEncoder.matches("plain", "HASH")).thenReturn(true);

    assertThat(authService.validateResetToken("plain")).isTrue();
  }

  @Test
  void validateResetToken_shouldReturnFalse_whenNoTokenMatches() {
    ForgotPasswordToken badtoken = ForgotPasswordToken.builder().tokenHash("HASH").build();
    when(forgotPasswordTokenRepository.findAllByUsedFalseAndExpiresAtAfter(
            any(LocalDateTime.class)))
        .thenReturn(List.of(badtoken));
    when(passwordEncoder.matches("plain", "HASH")).thenReturn(false);

    assertThat(authService.validateResetToken("plain")).isFalse();
  }

  @Test
  void resetPassword_shouldThrow400_whenTokenInvalidOrExpired() {
    when(forgotPasswordTokenRepository.findAllByUsedFalseAndExpiresAtAfter(
            any(LocalDateTime.class)))
        .thenReturn(List.of());

    ResetPasswordRequest request = resetRequest("badtoken", "Owner1234!");

    assertThatThrownBy(() -> authService.resetPassword(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo("The token is invalid or has expired");
            });

    verify(userRepository, never()).save(any());
    verify(forgotPasswordTokenRepository, never()).deleteAllByUser(any());
  }

  @ParameterizedTest(name = "Should throw 400 when password is \"{0}\" because {1}")
  @MethodSource("invalidPasswordProvider")
  void resetPassword_shouldThrow400_whenPasswordPolicyFails(
      String invalidPassword, String expectedErrorMessage) {
    User user = User.builder().email("owner@pokiepaws.pl").password("OLD").build();
    ForgotPasswordToken resettoken = validForgotToken(user, "HASHED");

    when(forgotPasswordTokenRepository.findAllByUsedFalseAndExpiresAtAfter(
            any(LocalDateTime.class)))
        .thenReturn(List.of(resettoken));
    when(passwordEncoder.matches("passwordresettoken", "HASHED")).thenReturn(true);

    ResetPasswordRequest request = resetRequest("passwordresettoken", invalidPassword);

    assertThatThrownBy(() -> authService.resetPassword(request))
        .isInstanceOf(ResponseStatusException.class)
        .satisfies(
            ex -> {
              ResponseStatusException rse = (ResponseStatusException) ex;
              assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
              assertThat(rse.getReason()).isEqualTo(expectedErrorMessage);
            });

    verify(userRepository, never()).save(any());
    verify(forgotPasswordTokenRepository, never()).deleteAllByUser(any());
  }

  private static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments>
      invalidPasswordProvider() {
    return java.util.stream.Stream.of(
        org.junit.jupiter.params.provider.Arguments.of(
            "lowercase1234!", "The password must contain at least one uppercase letter"),
        org.junit.jupiter.params.provider.Arguments.of(
            "Aa1!", "The password must be at least 8 characters long"),
        org.junit.jupiter.params.provider.Arguments.of(
            "Password!", "The password must contain a number"),
        org.junit.jupiter.params.provider.Arguments.of(
            "Password1234", "The password must contain a special character"),
        org.junit.jupiter.params.provider.Arguments.of(
            "owner@pokiepaws.plA1!", "The password must not contain an email address"));
  }

  @Test
  void resetPassword_shouldUpdatePassword_whenAllValid() {
    User user = User.builder().email("newcorrectpassword@pokiepaws.pl").password("OLD").build();
    ForgotPasswordToken passwordresettoken = validForgotToken(user, "HASH");

    when(forgotPasswordTokenRepository.findAllByUsedFalseAndExpiresAtAfter(
            any(LocalDateTime.class)))
        .thenReturn(List.of(passwordresettoken));
    when(passwordEncoder.matches("token", "HASH")).thenReturn(true);
    when(passwordEncoder.encode("NewPassword1234!")).thenReturn("NEW_HASH");

    ResetPasswordRequest request = resetRequest("token", "NewPassword1234!");

    authService.resetPassword(request);

    assertThat(user.getPassword()).isEqualTo("NEW_HASH");
    verify(userRepository).save(user);
    verify(forgotPasswordTokenRepository).deleteAllByUser(user);
  }
}
