package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.auth.AuthRequest;
import com.pokiepaws.api.dto.auth.AuthResponse;
import com.pokiepaws.api.dto.auth.RegisterRequest;
import com.pokiepaws.api.dto.auth.ResetPasswordRequest;
import com.pokiepaws.api.models.EmailVerificationToken;
import com.pokiepaws.api.models.ForgotPasswordToken;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.EmailVerificationTokenRepository;
import com.pokiepaws.api.repositories.ForgotPasswordTokenRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.security.JwtService;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

  @Value("${app.base-url}")
  private String baseUrl;

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

  public AuthResponse login(AuthRequest request) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!user.isEmailVerified()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, PLEASE_VERIFY_EMAIL_FIRST);
    }

    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole().name())
            .build();

    String token = jwtService.generateToken(userDetails);
    return new AuthResponse(token, user.getEmail(), user.getRole().name());
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
