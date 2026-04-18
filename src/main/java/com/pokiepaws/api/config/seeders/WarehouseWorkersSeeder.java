package com.pokiepaws.api.config.seeders;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.models.WarehouseWorker;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.WarehouseRepository;
import com.pokiepaws.api.repositories.WarehouseWorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class WarehouseWorkersSeeder implements Seeder {

  private final UserRepository userRepository;
  private final WarehouseRepository warehouseRepository;
  private final WarehouseWorkerRepository warehouseWorkerRepository;
  private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

  @Override
  public int order() {
    return 50;
  }

  @Override
  @Transactional
  public void seed() {
    Warehouse warehouse =
        warehouseRepository
            .findByWarehouseNameIgnoreCase(WarehousesSeeder.WAREHOUSE_MAIN)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        NOT_FOUND, "Warehouse not seeded: " + WarehousesSeeder.WAREHOUSE_MAIN));

    createWorkerIfMissing(
        "wh1@pokiepaws.pl", "Worker1234!", "Tomasz", "Magazynowy", "+48700200201", warehouse);

    createWorkerIfMissing(
        "wh2@pokiepaws.pl", "Worker1234!", "Monika", "Paczkowa", "+48700200202", warehouse);

    createWorkerIfMissing(
        "wh3@pokiepaws.pl", "Worker1234!", "Karol", "Logistyczny", "+48700200203", warehouse);

    log.info("Warehouse workers seeded.");
  }

  private void createWorkerIfMissing(
      String email,
      String rawPassword,
      String firstName,
      String lastName,
      String phoneNumber,
      Warehouse warehouse) {

    if (userRepository.existsByEmail(email)) return;

    User user =
        userRepository.save(
            User.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.WAREHOUSE)
                .emailVerified(true)
                .active(true)
                .build());

    warehouseWorkerRepository.save(
        WarehouseWorker.builder()
            .user(user)
            .warehouse(warehouse)
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phoneNumber)
            .active(true)
            .build());
  }
}
