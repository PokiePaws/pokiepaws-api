package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.config.datainit.dto.ClinicSeedDto;
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
@Profile({"dev", "local", "prod"})
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
        ClinicSeedDto.builder()
            .name("PokiePaws Warszawa")
            .regon("123456789")
            .nip("5260210488")
            .email("warszawa@pokiepawsclinic.pl")
            .phone("+48888555441")
            .street("Kwiatowa")
            .houseNumber("10")
            .postalCode("00-001")
            .city("Warszawa")
            .country("Poland")
            .workingHours("Pon-Pt 08:00-18:00")
            .build());

    getOrCreateClinic(
        ClinicSeedDto.builder()
            .name("PokiePaws Kraków")
            .regon("123456780")
            .nip("6762461659")
            .email("krakow@pokiepawsclinic.pl")
            .phone("+48888555442")
            .street("Leśna")
            .houseNumber("5")
            .apartmentNumber("12")
            .postalCode("30-002")
            .city("Kraków")
            .country("Poland")
            .workingHours("Pon-Sob 09:00-17:00")
            .build());

    log.info("Clinics seeded.");
  }

  private void getOrCreateClinic(ClinicSeedDto dto) {
    clinicRepository
        .findByClinicNameIgnoreCase(dto.getName())
        .orElseGet(
            () ->
                clinicRepository.save(
                    Clinic.builder()
                        .clinicName(dto.getName())
                        .regon(dto.getRegon())
                        .nip(dto.getNip())
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
                        .street(dto.getStreet())
                        .houseNumber(dto.getHouseNumber())
                        .apartmentNumber(dto.getApartmentNumber())
                        .postalCode(dto.getPostalCode())
                        .city(dto.getCity())
                        .country(dto.getCountry())
                        .workingHours(dto.getWorkingHours())
                        .active(true)
                        .build()));
  }
}
