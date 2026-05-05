package com.pokiepaws.api.dto.animal;

import com.pokiepaws.api.models.Animal;
import java.time.LocalDate;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnimalResponse {
  private Long id;
  private String name;
  private String species;
  private String breed;
  private Animal.Gender gender;
  private String color;
  private String microchipNumber;
  private Double weight;
  private LocalDate birthDate;
  private String notes;
}
