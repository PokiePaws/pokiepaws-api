package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.config.datainit.dto.WarehouseSeedDto;
import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.repositories.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class WarehousesSeeder implements Seeder {

  public static final String WAREHOUSE_MAIN = "PokiePaws Central Warehouse";
  private static final String DEFAULT_COUNTRY = "Poland";

  private final WarehouseRepository warehouseRepository;

  @Override
  public int order() {
    return 40;
  }

  @Override
  @Transactional
  public void seed() {
    createWarehouseIfMissing(
        WarehouseSeedDto.builder()
            .name(WAREHOUSE_MAIN)
            .email("warehouse@pokiepaws.pl")
            .phone("+48700200200")
            .nip("5250000000")
            .regon("000000000")
            .street("Przemysłowa")
            .houseNumber("1")
            .postalCode("00-450")
            .city("Warszawa")
            .country(DEFAULT_COUNTRY)
            .workingHours("Pon-Pt 07:00-15:00")
            .build());

    log.info("Warehouses seeded.");
  }

  private void createWarehouseIfMissing(WarehouseSeedDto dto) {
    warehouseRepository
        .findByWarehouseNameIgnoreCase(dto.getName())
        .orElseGet(
            () ->
                warehouseRepository.save(
                    Warehouse.builder()
                        .warehouseName(dto.getName())
                        .email(dto.getEmail())
                        .nip(dto.getNip())
                        .regon(dto.getRegon())
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
