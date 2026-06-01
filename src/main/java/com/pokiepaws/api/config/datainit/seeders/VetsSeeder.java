package com.pokiepaws.api.config.datainit.seeders;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.pokiepaws.api.config.datainit.dto.VetSeedDto;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local", "prod"})
public class VetsSeeder implements Seeder {

  private static final String CLINIC_WARSAW = "PokiePaws Warszawa";
  private static final String CLINIC_CRACOW = "PokiePaws Kraków";

  @Value("${DEFAULT_VET_PASSWORD:Vet1234!}")
  private String defaultVetPassword;

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
    createVetIfMissing(
        VetSeedDto.builder()
            .email("pokiepaws.vet1@protonmail.com")
            .password(defaultVetPassword)
            .firstName("Marta")
            .lastName("Robaczek")
            .phone("+48600600601")
            .npwz("12345")
            .specialization("Surgery")
            .clinicName(CLINIC_WARSAW)
            .build());

    createVetIfMissing(
        VetSeedDto.builder()
            .email("vet2@pokiepaws.pl")
            .password(defaultVetPassword)
            .firstName("Piotr")
            .lastName("Szczepionkowski")
            .phone("+48600600602")
            .npwz("12346")
            .specialization("Dermatology")
            .clinicName(CLINIC_CRACOW)
            .build());

    createVetIfMissing(
        VetSeedDto.builder()
            .email("vet3@pokiepaws.pl")
            .password(defaultVetPassword)
            .firstName("Aleksandra")
            .lastName("Łapka")
            .phone("+48600600603")
            .npwz("12347")
            .specialization("Diagnostics")
            .clinicName(CLINIC_CRACOW)
            .build());

    log.info("Vets seeded.");
  }

  private void createVetIfMissing(VetSeedDto dto) {
    if (userRepository.existsByEmail(dto.getEmail())) return;
    if (vetRepository.existsByNpwz(dto.getNpwz())) return;

    Clinic clinic =
        clinicRepository
            .findByClinicNameIgnoreCase(dto.getClinicName())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        NOT_FOUND, "Clinic not seeded: " + dto.getClinicName()));

    User user =
        userRepository.save(
            User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.VET)
                .emailVerified(true)
                .active(true)
                .failedLoginAttempts(0)
                .build());

    vetRepository.save(
        Vet.builder()
            .user(user)
            .clinic(clinic)
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phone(dto.getPhone())
            .npwz(dto.getNpwz())
            .specialization(dto.getSpecialization())
            .build());
  }
}
