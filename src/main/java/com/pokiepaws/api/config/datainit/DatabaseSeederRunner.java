package com.pokiepaws.api.config.datainit;

import com.pokiepaws.api.config.datainit.seeders.Seeder;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local", "prod"})
public class DatabaseSeederRunner implements CommandLineRunner {

  private final List<Seeder> seeders;

  @Override
  public void run(String... args) {
    log.info("Starting database seeding process...");

    seeders.stream()
        .sorted(Comparator.comparingInt(Seeder::order))
        .forEach(
            seeder -> {
              log.info("Executing seeder: {}", seeder.getClass().getSimpleName());
              seeder.seed();
            });

    log.info("Database seeding completed successfully.");
  }
}
