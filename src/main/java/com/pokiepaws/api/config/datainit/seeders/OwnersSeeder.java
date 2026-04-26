package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.config.datainit.dto.OwnerSeedDto;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value; // Ważny import!
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class OwnersSeeder implements Seeder {

  private final UserRepository userRepository;
  private final OwnerRepository ownerRepository;
  private final PasswordEncoder passwordEncoder;

  private static final String DEFAULT_COUNTRY = "Poland";

  @Value("${DEFAULT_OWNER_PASSWORD:Owner1234!}")
  private String defaultOwnerPassword;

  @Override
  public int order() {
    return 20;
  }

  @Override
  @Transactional
  public void seed() {
    createOwnerIfMissing(
        OwnerSeedDto.builder()
            .email("owner1@pokiepaws.pl")
            .password(defaultOwnerPassword)
            .firstName("Anna")
            .lastName("Kowalska")
            .phoneNumber("+48500500501")
            .street("Kwiatowa")
            .houseNumber("10")
            .postalCode("00-001")
            .city("Warszawa")
            .country(DEFAULT_COUNTRY)
            .build());

    createOwnerIfMissing(
        OwnerSeedDto.builder()
            .email("owner2@pokiepaws.pl")
            .password(defaultOwnerPassword)
            .firstName("Jan")
            .lastName("Nowak")
            .phoneNumber("+48500500502")
            .street("Leśna")
            .houseNumber("5")
            .apartmentNumber("12")
            .postalCode("30-002")
            .city("Kraków")
            .country(DEFAULT_COUNTRY)
            .build());

    createOwnerIfMissing(
        OwnerSeedDto.builder()
            .email("owner3@pokiepaws.pl")
            .password(defaultOwnerPassword)
            .firstName("Kasia")
            .lastName("Zielińska")
            .phoneNumber("+48500500503")
            .street("Polna")
            .houseNumber("7")
            .postalCode("80-003")
            .city("Gdańsk")
            .country(DEFAULT_COUNTRY)
            .build());

    log.info("Owners seeded.");
  }

  private void createOwnerIfMissing(OwnerSeedDto dto) {
    if (userRepository.existsByEmail(dto.getEmail())) return;

    User user =
        userRepository.save(
            User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.OWNER)
                .emailVerified(true)
                .active(true)
                .build());

    ownerRepository.save(
        Owner.builder()
            .user(user)
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phoneNumber(dto.getPhoneNumber())
            .street(dto.getStreet())
            .houseNumber(dto.getHouseNumber())
            .apartmentNumber(dto.getApartmentNumber())
            .postalCode(dto.getPostalCode())
            .city(dto.getCity())
            .country(dto.getCountry())
            .build());
  }
}
