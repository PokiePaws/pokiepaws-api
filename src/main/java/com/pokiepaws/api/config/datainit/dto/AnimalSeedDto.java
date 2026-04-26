package com.pokiepaws.api.config.datainit.dto;

import com.pokiepaws.api.models.Animal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnimalSeedDto {
  private final String ownerEmail;
  private final String name;
  private final String species;
  private final String breed;
  private final Animal.Gender gender;
  private final String color;
  private final String microchipNumber;
  private final Double weight;
  private final LocalDate birthDate;
  private final String notes;
}
