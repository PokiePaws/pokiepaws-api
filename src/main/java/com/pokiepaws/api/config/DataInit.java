package com.pokiepaws.api.config;

import com.pokiepaws.api.config.datainit.seeders.Seeder;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInit implements ApplicationRunner {

  private final List<Seeder> seeders;

  public DataInit(List<Seeder> seeders) {
    this.seeders = List.copyOf(seeders);
  }

  @Override
  public void run(ApplicationArguments args) {
    seeders.stream()
        .sorted(Comparator.comparingInt(Seeder::order))
        .forEach(
            seeder -> {
              log.info("Running seeder: {}", seeder.getClass().getSimpleName());
              seeder.seed();
            });
  }
}
