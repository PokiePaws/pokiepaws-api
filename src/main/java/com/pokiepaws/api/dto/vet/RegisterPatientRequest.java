package com.pokiepaws.api.dto.vet;

import com.pokiepaws.api.models.Animal;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RegisterPatientRequest {

  // owner info
  @NotBlank @Email private String ownerEmail;

  @NotBlank private String ownerFirstName;

  @NotBlank private String ownerLastName;

  private String ownerPhone;

  // animal info
  @NotBlank private String animalName;

  @NotBlank private String animalSpecies;

  private String animalBreed;

  @NotNull private Animal.Gender animalGender;

  private String animalColor;
  private String animalMicrochipNumber;
  private Double animalWeight;
  private LocalDate animalBirthDate;
  private String animalNotes;
}
