package com.pokiepaws.api.config.datainit.seeders;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.pokiepaws.api.config.datainit.dto.WarehouseWorkerSeedDto;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.models.WarehouseWorker;
import com.pokiepaws.api.repositories.UserRepository;
import com.pokiepaws.api.repositories.WarehouseRepository;
import com.pokiepaws.api.repositories.WarehouseWorkerRepository;
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
@Profile({"dev", "local"})
public class WarehouseWorkersSeeder implements Seeder {

  private final UserRepository userRepository;
  private final WarehouseRepository warehouseRepository;
  private final WarehouseWorkerRepository warehouseWorkerRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${DEAFULT-WORKER-PASSWORD:Vet1234!}")
  private String defaultWorkerPassword;

  @Override
  public int order() {
    return 50;
  }

  @Override
  @Transactional
  public void seed() {
    createWorkerIfMissing(
        WarehouseWorkerSeedDto.builder()
            .email("wh1@pokiepaws.pl")
            .password(defaultWorkerPassword)
            .firstName("Tomasz")
            .lastName("Magazynowy")
            .phoneNumber("+48700200201")
            .warehouseName(WarehousesSeeder.WAREHOUSE_MAIN)
            .build());

    createWorkerIfMissing(
        WarehouseWorkerSeedDto.builder()
            .email("wh2@pokiepaws.pl")
            .password(defaultWorkerPassword)
            .firstName("Monika")
            .lastName("Paczkowa")
            .phoneNumber("+48700200202")
            .warehouseName(WarehousesSeeder.WAREHOUSE_MAIN)
            .build());

    createWorkerIfMissing(
        WarehouseWorkerSeedDto.builder()
            .email("wh3@pokiepaws.pl")
            .password(defaultWorkerPassword)
            .firstName("Karol")
            .lastName("Logistyczny")
            .phoneNumber("+48700200203")
            .warehouseName(WarehousesSeeder.WAREHOUSE_MAIN)
            .build());

    log.info("Warehouse workers seeded.");
  }

  private void createWorkerIfMissing(WarehouseWorkerSeedDto dto) {
    if (userRepository.existsByEmail(dto.getEmail())) return;

    Warehouse warehouse =
        warehouseRepository
            .findByWarehouseNameIgnoreCase(dto.getWarehouseName())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        NOT_FOUND, "Warehouse not seeded: " + dto.getWarehouseName()));

    User user =
        userRepository.save(
            User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.WAREHOUSE)
                .emailVerified(true)
                .active(true)
                .build());

    warehouseWorkerRepository.save(
        WarehouseWorker.builder()
            .user(user)
            .warehouse(warehouse)
            .firstName(dto.getFirstName())
            .lastName(dto.getLastName())
            .phoneNumber(dto.getPhoneNumber())
            .active(true)
            .build());
  }
}
