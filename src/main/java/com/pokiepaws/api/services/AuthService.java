package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.AuthRequest;
import com.pokiepaws.api.dto.AuthResponse;
import com.pokiepaws.api.dto.RegisterRequest;
import com.pokiepaws.api.dto.ResetPasswordRequest;
import com.pokiepaws.api.models.EmailVerificationToken;
import com.pokiepaws.api.models.ForgotPasswordToken;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.EmailVerificationTokenRepository;
import com.pokiepaws.api.repositories.ForgotPasswordTokenRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final ForgotPasswordTokenRepository forgotPasswordTokenRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Rejestruje użytkownika, ale NIE loguje go automatycznie.
     * Wymaga potwierdzenia adresu e-mail przed pierwszym logowaniem.
     */
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .street(request.getStreet())
                .houseNumber(request.getHouseNumber())
                .apartmentNumber(request.getApartmentNumber())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .role(Role.OWNER)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false) // Użytkownik niezweryfikowany
                .active(true)
                .build();

        userRepository.save(user);

        // Generowanie tokena weryfikacyjnego
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = EmailVerificationToken.builder()
                .token(verificationToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();
        tokenRepository.save(emailToken);

        // Wysyłka maila
        emailService.sendVerificationEmail(user.getEmail(), verificationToken, baseUrl);
    }

    /**
     * Loguje użytkownika tylko wtedy, gdy jego e-mail jest zweryfikowany.
     */
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Sprawdzenie czy e-mail został potwierdzony
        if (!user.isEmailVerified()) {
            throw new RuntimeException("Proszę najpierw potwierdzić adres e-mail.");
        }

        // Autentykacja Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();

        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public String verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (verificationToken.isUsed()) {
            throw new RuntimeException("The token has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The token has expired");
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
        userRepository.findByEmail(email).ifPresent(user -> {
            forgotPasswordTokenRepository.deleteAllByUser(user);
            String plainToken = generateSecureToken();

            ForgotPasswordToken resetToken = ForgotPasswordToken.builder()
                    .tokenHash(passwordEncoder.encode(plainToken))
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusMinutes(15))
                    .used(false)
                    .build();

            forgotPasswordTokenRepository.save(resetToken);
            emailService.sendForgotPasswordEmail(user.getEmail(), plainToken, baseUrl);
        });
    }

    @Transactional(readOnly = true)
    public boolean validateResetToken(String plainToken) {
        return forgotPasswordTokenRepository
                .findAllByUsedFalseAndExpiresAtAfter(LocalDateTime.now())
                .stream()
                .anyMatch(t -> passwordEncoder.matches(plainToken, t.getTokenHash()));
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        ForgotPasswordToken resetToken = forgotPasswordTokenRepository
                .findAllByUsedFalseAndExpiresAtAfter(LocalDateTime.now())
                .stream()
                .filter(t -> passwordEncoder.matches(request.getToken(), t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("The token is invalid or has expired"));

        User user = resetToken.getUser();
        validatePasswordPolicy(request.getNewPassword(), user);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        forgotPasswordTokenRepository.deleteAllByUser(user);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[36];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validatePasswordPolicy(String password, User user) {
        if (password.length() < 8)
            throw new RuntimeException("The password must be at least 8 characters long");
        if (!password.matches(".*[A-Z].*"))
            throw new RuntimeException("The password must contain at least one uppercase letter");
        if (!password.matches(".*[0-9].*"))
            throw new RuntimeException("The password must contain a number");
        if (!password.matches(".*[!@#$%^&*()].*"))
            throw new RuntimeException("The password must contain a special character");
        if (password.toLowerCase().contains(user.getEmail().toLowerCase()))
            throw new RuntimeException("The password must not contain an email address");
    }

    public String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}