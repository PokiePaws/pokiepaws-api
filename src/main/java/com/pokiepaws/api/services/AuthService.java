package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.auth.AuthRequest;
import com.pokiepaws.api.dto.auth.AuthResponse;
import com.pokiepaws.api.dto.auth.RegisterRequest;
import com.pokiepaws.api.dto.auth.ResetPasswordRequest;
import com.pokiepaws.api.models.ActivityLog.LogType;
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
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private static final String EMAIL_ALREADY_IN_USE = "Email already in use";
  private static final String PLEASE_VERIFY_EMAIL_FIRST = "Please verify your email address first.";
  private static final String INVALID_TOKEN = "Invalid token";
  private static final String TOKEN_ALREADY_USED = "The token has already been used";
  private static final String TOKEN_EXPIRED = "The token has expired";
  private static final String RESET_TOKEN_INVALID_OR_EXPIRED =
      "The token is invalid or has expired";
  private static final String ACCOUNT_INACTIVE = "The account is inactive";
  private static final String MFA_TOKEN_INVALID_OR_EXPIRED =
      "The 2FA token is invalid or has expired";
  private static final String MFA_RATE_LIMIT_EXCEEDED =
      "Too many 2FA requests. Please try again later.";
  private static final String REFRESH_TOKEN_INVALID_OR_EXPIRED =
      "The refresh token is invalid or has expired";
  private static final String INVALID_CREDENTIALS = "Invalid email or password";
  private static final String ACCOUNT_TEMPORARILY_LOCKED =
      "The account is temporarily locked. Please try again later.";
  private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
  private static final int LOGIN_LOCK_MINUTES = 15;

  private static final String PASSWORD_MIN_LENGTH =
      "The password must be at least 8 characters long";
  private static final String PASSWORD_NEEDS_UPPERCASE =
      "The password must contain at least one uppercase letter";
  private static final String PASSWORD_NEEDS_NUMBER = "The password must contain a number";
  private static final String PASSWORD_NEEDS_SPECIAL =
      "The password must contain a special character";
  private static final String PASSWORD_MUST_NOT_CONTAIN_EMAIL =
      "The password must not contain an email address";

  private final Clock clock;
  private final UserRepository userRepository;
  private final OwnerRepository ownerRepository;

  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final EmailVerificationTokenRepository tokenRepository;
  private final EmailService emailService;
  private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final MfaTokenRepository mfaTokenRepository;
  private final ActivityLogService activityLogService;

  @Value("${app.base-url}")
  private String baseUrl;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.refresh-token.expiration-ms:604800000}")
  private long refreshTokenExpirationMs;

  @Transactional
  public void register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, EMAIL_ALREADY_IN_USE);
    }

    User user =
        User.builder()
            .role(Role.OWNER)
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .emailVerified(false)
            .active(true)
            .build();

    user = userRepository.save(user);

    Owner owner =
        Owner.builder()
            .user(user)
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .street(request.getStreet())
            .houseNumber(request.getHouseNumber())
            .apartmentNumber(request.getApartmentNumber())
            .city(request.getCity())
            .postalCode(request.getPostalCode())
            .country(request.getCountry())
            .build();

    ownerRepository.save(owner);
    String verificationToken = UUID.randomUUID().toString();
    EmailVerificationToken emailToken =
        EmailVerificationToken.builder()
            .token(verificationToken)
            .user(user)
            .expiresAt(LocalDateTime.now(clock).plusHours(24))
            .used(false)
            .build();
    tokenRepository.save(emailToken);

    emailService.sendVerificationEmail(user.getEmail(), verificationToken, baseUrl);
  }

  @Transactional
  public AuthResponse login(AuthRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () -> {
                  activityLogService.logFor(
                      request.getEmail(), LogType.login, "Failed login: unknown account", null);
                  return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
                });

    if (!user.isEmailVerified()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, PLEASE_VERIFY_EMAIL_FIRST);
    }
    if (!user.isActive()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCOUNT_INACTIVE);
    }
    if (isLoginLocked(user)) {
      activityLogService.logFor(
          user.getEmail(), LogType.login, "Failed login: account temporarily locked", null);
      throw new ResponseStatusException(HttpStatus.LOCKED, ACCOUNT_TEMPORARILY_LOCKED);
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    } catch (AuthenticationException ex) {
      recordFailedLogin(user);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
    }

    resetFailedLogins(user);

    if (requiresMfa(user)) {
      createAndSendMfaToken(user);
      activityLogService.logFor(user.getEmail(), LogType.login, "2FA challenge sent", null);
      return AuthResponse.mfaRequired(user.getEmail(), user.getRole().name());
    }

    AuthResponse response = issueTokens(user);
    activityLogService.logFor(user.getEmail(), LogType.login, "Login successful", null);
    return response;
  }

  @Transactional
  public AuthResponse verifyMfa(String plainToken) {
    MfaToken mfaToken =
        mfaTokenRepository.findAllByUsedFalseAndExpiresAtAfter(LocalDateTime.now(clock)).stream()
            .filter(token -> passwordEncoder.matches(plainToken, token.getTokenHash()))
            .findFirst()
            .orElseThrow(
                () -> {
                  activityLogService.logFor(
                      "unknown", LogType.login, "2FA verification failed", null);
                  return new ResponseStatusException(
                      HttpStatus.BAD_REQUEST, MFA_TOKEN_INVALID_OR_EXPIRED);
                });

    User user = mfaToken.getUser();
    if (!user.isActive()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCOUNT_INACTIVE);
    }

    mfaToken.setUsed(true);
    mfaTokenRepository.save(mfaToken);

    AuthResponse response = issueTokens(user);
    activityLogService.logFor(user.getEmail(), LogType.login, "2FA verification successful", null);
    return response;
  }

  @Transactional
  public AuthResponse refresh(String plainRefreshToken) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findAllByRevokedFalseAndExpiresAtAfter(LocalDateTime.now(clock))
            .stream()
            .filter(token -> passwordEncoder.matches(plainRefreshToken, token.getTokenHash()))
            .findFirst()
            .orElseThrow(
                () -> {
                  activityLogService.logFor("unknown", LogType.login, "Refresh token failed", null);
                  return new ResponseStatusException(
                      HttpStatus.UNAUTHORIZED, REFRESH_TOKEN_INVALID_OR_EXPIRED);
                });

    User user = refreshToken.getUser();
    if (!user.isActive()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCOUNT_INACTIVE);
    }

    refreshToken.setRevoked(true);
    refreshTokenRepository.save(refreshToken);

    AuthResponse response = issueTokens(user);
    activityLogService.logFor(user.getEmail(), LogType.login, "Refresh token rotated", null);
    return response;
  }

  @Transactional
  public void logout(String plainRefreshToken) {
    refreshTokenRepository.findAllByRevokedFalseAndExpiresAtAfter(LocalDateTime.now(clock)).stream()
        .filter(token -> passwordEncoder.matches(plainRefreshToken, token.getTokenHash()))
        .findFirst()
        .ifPresent(
            token -> {
              token.setRevoked(true);
              refreshTokenRepository.save(token);
              activityLogService.logFor(token.getUser().getEmail(), LogType.login, "Logout", null);
            });
  }

  @Transactional
  public String verifyEmail(String token) {
    EmailVerificationToken verificationToken =
        tokenRepository
            .findByToken(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_TOKEN));

    if (verificationToken.isUsed()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TOKEN_ALREADY_USED);
    }

    if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, TOKEN_EXPIRED);
    }

    User user = verificationToken.getUser();
    user.setEmailVerified(true);
    userRepository.save(user);

    verificationToken.setUsed(true);
    tokenRepository.save(verificationToken);

    return "Your email address has been confirmed! You can now log in.";
  }

  @Transactional
  public void forgotPassword(String email) {
    userRepository
        .findByEmail(email)
        .ifPresent(
            user -> {
              forgotPasswordTokenRepository.deleteAllByUser(user);
              String plainToken = generateSecureToken();

              ForgotPasswordToken resetToken =
                  ForgotPasswordToken.builder()
                      .tokenHash(passwordEncoder.encode(plainToken))
                      .user(user)
                      .expiresAt(LocalDateTime.now(clock).plusMinutes(15))
                      .used(false)
                      .build();

              forgotPasswordTokenRepository.save(resetToken);
              emailService.sendForgotPasswordEmail(user.getEmail(), plainToken, baseUrl);
            });
  }

  @Transactional(readOnly = true)
  public boolean validateResetToken(String plainToken) {
    return forgotPasswordTokenRepository
        .findAllByUsedFalseAndExpiresAtAfter(LocalDateTime.now(clock))
        .stream()
        .anyMatch(t -> passwordEncoder.matches(plainToken, t.getTokenHash()));
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    ForgotPasswordToken resetToken =
        forgotPasswordTokenRepository
            .findAllByUsedFalseAndExpiresAtAfter(LocalDateTime.now(clock))
            .stream()
            .filter(t -> passwordEncoder.matches(request.getToken(), t.getTokenHash()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, RESET_TOKEN_INVALID_OR_EXPIRED));

    User user = resetToken.getUser();
    validatePasswordPolicy(request.getNewPassword(), user.getEmail());

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    forgotPasswordTokenRepository.deleteAllByUser(user);
  }

  private String generateSecureToken() {
    byte[] bytes = new byte[36];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private boolean requiresMfa(User user) {
    return user.getRole() == Role.ADMIN || user.getRole() == Role.VET;
  }

  private void createAndSendMfaToken(User user) {
    LocalDateTime now = LocalDateTime.now(clock);
    if (mfaTokenRepository.findAllByUserAndCreatedAtAfter(user, now.minusMinutes(10)).size() >= 3) {
      throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, MFA_RATE_LIMIT_EXCEEDED);
    }

    List<MfaToken> activeTokens = mfaTokenRepository.findAllByUserAndUsedFalse(user);
    activeTokens.forEach(token -> token.setUsed(true));
    mfaTokenRepository.saveAll(activeTokens);

    String plainToken = generateSecureToken();
    MfaToken mfaToken =
        MfaToken.builder()
            .user(user)
            .tokenHash(passwordEncoder.encode(plainToken))
            .expiresAt(now.plusMinutes(15))
            .used(false)
            .createdAt(now)
            .build();
    mfaTokenRepository.save(mfaToken);

    emailService.sendMfaLink(user.getEmail(), plainToken, frontendUrl);
  }

  private boolean isLoginLocked(User user) {
    return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now(clock));
  }

  private void recordFailedLogin(User user) {
    int attempts = user.getFailedLoginAttempts() + 1;
    user.setFailedLoginAttempts(attempts);
    if (attempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
      user.setLockedUntil(LocalDateTime.now(clock).plusMinutes(LOGIN_LOCK_MINUTES));
    }
    userRepository.save(user);
    activityLogService.logFor(user.getEmail(), LogType.login, "Failed login", null);
  }

  private void resetFailedLogins(User user) {
    if (user.getFailedLoginAttempts() == 0 && user.getLockedUntil() == null) {
      return;
    }
    user.setFailedLoginAttempts(0);
    user.setLockedUntil(null);
    userRepository.save(user);
  }

  private AuthResponse issueTokens(User user) {
    UserDetails userDetails = buildUserDetails(user);
    String accessToken = jwtService.generateToken(userDetails);
    String refreshToken = createRefreshToken(user);
    return AuthResponse.authenticated(
        accessToken, refreshToken, user.getEmail(), user.getRole().name());
  }

  private String createRefreshToken(User user) {
    String plainToken = generateSecureToken();
    LocalDateTime now = LocalDateTime.now(clock);
    RefreshToken refreshToken =
        RefreshToken.builder()
            .user(user)
            .tokenHash(passwordEncoder.encode(plainToken))
            .expiresAt(now.plus(Duration.ofMillis(refreshTokenExpirationMs)))
            .revoked(false)
            .createdAt(now)
            .build();
    refreshTokenRepository.save(refreshToken);
    return plainToken;
  }

  private UserDetails buildUserDetails(User user) {
    return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
        .password(user.getPassword())
        .authorities("ROLE_" + user.getRole().name())
        .build();
  }

  private void validatePasswordPolicy(String password, String email) {
    if (password.length() < 8) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_MIN_LENGTH);
    }
    if (!password.matches(".*[A-Z].*")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_NEEDS_UPPERCASE);
    }
    if (!password.matches(".*\\d.*")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_NEEDS_NUMBER);
    }
    if (!password.matches(".*[!@#$%^&*()].*")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_NEEDS_SPECIAL);
    }

    if (email != null) {
      String passwordLower = password.toLowerCase(Locale.ROOT);
      String emailLower = email.toLowerCase(Locale.ROOT);
      if (passwordLower.contains(emailLower)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PASSWORD_MUST_NOT_CONTAIN_EMAIL);
      }
    }
  }
}
