package com.pokiepaws.api.config.seeders;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class VetsSeeder implements Seeder {

  private static final String CLINIC_WARSAW = "PokiePaws Warszawa";
  private static final String CLINIC_CRACOW = "PokiePaws Kraków";

  private final UserRepository userRepository;
  private final VetRepository vetRepository;
  private final ClinicRepository clinicRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public int order() {
    return 30;
  }

  @Override
  @Transactional
  public void seed() {
    Clinic warsaw =
        clinicRepository
            .findByClinicNameIgnoreCase(CLINIC_WARSAW)
            .orElseThrow(
                () ->
                    new ResponseStatusException(NOT_FOUND, "Clinic not seeded: " + CLINIC_WARSAW));

    Clinic cracow =
        clinicRepository
            .findByClinicNameIgnoreCase(CLINIC_CRACOW)
            .orElseThrow(
                () ->
                    new ResponseStatusException(NOT_FOUND, "Clinic not seeded: " + CLINIC_CRACOW));

    createVetIfMissing(
        "vet1@pokiepaws.pl",
        "Vet1234!",
        "Marta",
        "Robaczek",
        "+48600600601",
        "12345",
        "Surgery",
        warsaw);

    createVetIfMissing(
        "vet2@pokiepaws.pl",
        "Vet1234!",
        "Piotr",
        "Szczepionkowski",
        "+48600600602",
        "12346",
        "Dermatology",
        cracow);

    createVetIfMissing(
        "vet3@pokiepaws.pl",
        "Vet1234!",
        "Aleksandra",
        "Łapka",
        "+48600600603",
        "12347",
        "Diagnostics",
        cracow);

    log.info("Vets seeded.");
  }

  private void createVetIfMissing(
      String email,
      String rawPassword,
      String firstName,
      String lastName,
      String phone,
      String npwz,
      String specialization,
      Clinic clinic) {

    if (userRepository.existsByEmail(email)) return;
    if (vetRepository.existsByNpwz(npwz)) return;

    User user =
        userRepository.save(
            User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.VET)
                .emailVerified(true)
                .active(true)
                .build());

    vetRepository.save(
        Vet.builder()
            .user(user)
            .clinic(clinic)
            .firstName(firstName)
            .lastName(lastName)
            .phone(phone)
            .npwz(npwz)
            .specialization(specialization)
            .build());
  }
}
