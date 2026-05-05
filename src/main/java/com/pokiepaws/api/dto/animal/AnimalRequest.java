package com.pokiepaws.api.dto.animal;

import com.pokiepaws.api.models.Animal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalRequest {
  @NotBlank private String name;
  @NotBlank private String species;
  private String breed;
  @NotNull private Animal.Gender gender;
  private String color;
  private String microchipNumber;
  private Double weight;
  private LocalDate birthDate;
  private String notes;
}
