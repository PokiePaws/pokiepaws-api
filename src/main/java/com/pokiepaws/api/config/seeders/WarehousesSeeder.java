package com.pokiepaws.api.config.seeders;

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

  public static final String WAREHOUSE_MAIN = "PokiePaws Central Werehouse";

  private final WarehouseRepository warehouseRepository;

  @Override
  public int order() {
    return 40;
  }

  @Override
  @Transactional
  public void seed() {
    getOrCreateWarehouse(
        WAREHOUSE_MAIN,
        "warehouse@pokiepaws.pl",
        "+48700200200",
        "Przemysłowa",
        "1",
        null,
        "00-450",
        "Warszawa",
        "Poland",
        "Pon-Pt 07:00-15:00");

    log.info("Warehouse seeded.");
  }

  private Warehouse getOrCreateWarehouse(
      String name,
      String email,
      String phone,
      String street,
      String houseNumber,
      String apartmentNumber,
      String postalCode,
      String city,
      String country,
      String workingHours) {

    return warehouseRepository
        .findByWarehouseNameIgnoreCase(name)
        .orElseGet(
            () ->
                warehouseRepository.save(
                    Warehouse.builder()
                        .warehouseName(name)
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
