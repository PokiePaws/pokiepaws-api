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
        message.setSubject("PokiePaws — Potwierdź swój adres email");
        message.setText(
                "Witaj w PokiePaws! 🐾\n\n" +
                        "Kliknij poniższy link aby potwierdzić swój adres email:\n\n" +
                        baseUrl + "/api/auth/verify-email?token=" + token + "\n\n" +
                        "Pozdrawiamy,\nZespół PokiePaws"
        );
        mailSender.send(message);
    }
}