package com.pokiepaws.api.dto.vet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VetRequest {
  @NotBlank private String firstName;

  @NotBlank private String lastName;

  private String phone;

  @NotBlank private String npwz;

  private String specialization;

  @NotNull private Long clinicId;
}
