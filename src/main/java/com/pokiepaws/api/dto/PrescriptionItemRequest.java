package com.pokiepaws.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrescriptionItemRequest {

  @NotNull private Long productId;

  @Min(1)
  private int quantityPackages;

  private String dosage;
  private String treatmentTime;
}
