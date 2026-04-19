package com.pokiepaws.api.config.seeders;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.repositories.ClinicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class ClinicsSeeder implements Seeder {

  private final ClinicRepository clinicRepository;

  @Override
  public int order() {
    return 10;
  }

  @Override
  @Transactional
  public void seed() {
    getOrCreateClinic(
        "PokiePaws Warszawa",
        "123456789",
        "warszawa@pokiepawsclinic.pl",
        "+48888555441",
        "Kwiatowa",
        "10",
        null,
        "00-001",
        "Warszawa",
        "Poland",
        "Pon-Pt 08:00-18:00");

    getOrCreateClinic(
        "PokiePaws Kraków",
        "123456780",
        "krakow@pokiepawsclinic.pl",
        "+48888555442",
        "Leśna",
        "5",
        "12",
        "30-002",
        "Kraków",
        "Poland",
        "Pon-Sob 09:00-17:00");

    log.info("Clinics seeded.");
  }

  private Clinic getOrCreateClinic(
      String name,
      String regon,
      String email,
      String phone,
      String street,
      String houseNumber,
      String apartmentNumber,
      String postalCode,
      String city,
      String country,
      String workingHours) {

    return clinicRepository
        .findByClinicNameIgnoreCase(name)
        .orElseGet(
            () ->
                clinicRepository.save(
                    Clinic.builder()
                        .clinicName(name)
                        .regon(regon)
                        .email(email)
                        .phone(phone)
                        .street(street)
                        .houseNumber(houseNumber)
                        .apartmentNumber(apartmentNumber)
                        .postalCode(postalCode)
                        .city(city)
                        .country(country)
                        .workingHours(workingHours)
                        .active(true)
                        .build()));
  }
}
