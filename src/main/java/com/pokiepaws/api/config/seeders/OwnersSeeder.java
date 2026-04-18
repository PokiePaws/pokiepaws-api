package com.pokiepaws.api.config.seeders;

import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.OwnerRepository;
import com.pokiepaws.api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public int order() {
    return 20;
  }

  @Override
  @Transactional
  public void seed() {
    createOwnerIfMissing(
        "owner1@pokiepaws.pl",
        "Owner1234!",
        "Anna",
        "Kowalska",
        "+48500500501",
        "Kwiatowa",
        "10",
        null,
        "00-001",
        "Warszawa",
        "Poland");

    createOwnerIfMissing(
        "owner2@pokiepaws.pl",
        "Owner1234!",
        "Jan",
        "Nowak",
        "+48500500502",
        "Leśna",
        "5",
        "12",
        "30-002",
        "Kraków",
        "Poland");

    createOwnerIfMissing(
        "owner3@pokiepaws.pl",
        "Owner1234!",
        "Kasia",
        "Zielińska",
        "+48500500503",
        "Polna",
        "7",
        null,
        "80-003",
        "Gdańsk",
        "Poland");

    log.info("Owners seeded.");
  }

  private void createOwnerIfMissing(
      String email,
      String rawPassword,
      String firstName,
      String lastName,
      String phoneNumber,
      String street,
      String houseNumber,
      String apartmentNumber,
      String postalCode,
      String city,
      String country) {

    if (userRepository.existsByEmail(email)) return;

    User user =
        userRepository.save(
            User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.OWNER)
                .emailVerified(true)
                .active(true)
                .build());

    ownerRepository.save(
        Owner.builder()
            .user(user)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phoneNumber)
            .street(street)
            .houseNumber(houseNumber)
            .apartmentNumber(apartmentNumber)
            .postalCode(postalCode)
            .city(city)
            .country(country)
            .build());
  }
}
