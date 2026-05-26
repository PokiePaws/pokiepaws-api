package com.pokiepaws.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

  private static final String NO_REPLY_EMAIL = "noreply@pokiepaws.pl";

  private final JavaMailSender mailSender;

  public void sendVerificationEmail(String to, String token, String baseUrl) {
    sendEmail(
        to,
        "PokiePaws — Please confirm your email address",
        "Welcome to PokiePaws! 🐾\n\n"
            + "Click the link below to confirm your email address:\n\n"
            + baseUrl
            + "/api/auth/verify-email?token="
            + token
            + "\n\n"
            + "Best regards,\nThe PokiePaws team");
  }

  public void sendMfaLink(String to, String token, String frontendUrl) {
    sendEmail(
        to,
        "PokiePaws - Two-step verification",
        "Hello!\n\n"
            + "Click the link below to finish signing in to PokiePaws:\n\n"
            + frontendUrl
            + "/auth/verify?token="
            + token
            + "\n\n"
            + "This link expires in 15 minutes and can be used only once.\n\n"
            + "If you did not try to sign in, ignore this message.\n\n"
            + "Best regards,\nThe PokiePaws team");
  }

  private void sendEmail(String to, String subject, String text) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(NO_REPLY_EMAIL);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);
    mailSender.send(message);
  }
}
