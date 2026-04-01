package com.pokiepaws.api.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token, String baseUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@pokiepaws.pl");
        message.setTo(to);
        message.setSubject("PokiePaws — Please confirm your email address");
        message.setText(
                "Welcome to PokiePaws! 🐾\n\n" +
                        "Click the link below to confirm your email address:\n\n" +
                        baseUrl + "/api/auth/verify-email?token=" + token + "\n\n" +
                        "Best regards,\nThe PokiePaws team"
        );
        mailSender.send(message);
    }
    public void sendForgotPasswordEmail(String to, String token, String baseUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@pokiepaws.pl");
        message.setTo(to);
        message.setSubject("PokiePaws — Reset password");
        message.setText(
                "Hello! 🐾\n\n" +
                        "We have received a request to reset the password for your PokiePaws account.\n\n" +
                        "Click the link below to set a new password:\n\n" +
                        baseUrl + "/api/auth/reset-password?token=" + token + "\n\n" +
                        "If you haven’t requested a password reset, please ignore this message.\n\n" +
                        "Best regards,\nThe PokiePaws team"
        );
        mailSender.send(message);
    }
}