package com.pokiepaws.api.config.seeders;

import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class AdminSeeder implements Seeder {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.email}")
  private String adminEmail;

  @Value("${app.admin.password}")
  private String adminPassword;

  @Override
  public int order() {
    return 1;
  }

  @Override
  @Transactional
  public void seed() {
    if (userRepository.existsByEmail(adminEmail)) {
      log.info("The administrator already exists.");
      return;
    }

    User admin =
        User.builder()
            .email(adminEmail)
            .password(passwordEncoder.encode(adminPassword))
            .role(Role.ADMIN)
            .emailVerified(true)
            .active(true)
            .build();

    userRepository.save(admin);
    log.info("Administrator created: {}", adminEmail);
  }
}
