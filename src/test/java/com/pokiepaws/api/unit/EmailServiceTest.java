package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.pokiepaws.api.services.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

  @Mock JavaMailSender mailSender;

  private EmailService emailService;

  @BeforeEach
  void setUp() {
    emailService = new EmailService(mailSender);
  }

  @Test
  void sendVerificationEmail_shouldSendExpectedMessageWithVerificationLink() {
    emailService.sendVerificationEmail("owner@pokiepaws.pl", "TOKEN", "https://api.pokiepaws.pl");

    ArgumentCaptor<SimpleMailMessage> messageCaptor =
        ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage message = messageCaptor.getValue();
    assertThat(message.getFrom()).isEqualTo("noreply@pokiepaws.pl");
    assertThat(message.getTo()).containsExactly("owner@pokiepaws.pl");
    assertThat(message.getSubject()).contains("confirm your email address");
    assertThat(message.getText())
        .contains("https://api.pokiepaws.pl/api/auth/verify-email?token=TOKEN");
  }
}
