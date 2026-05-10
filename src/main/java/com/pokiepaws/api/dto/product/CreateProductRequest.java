package com.pokiepaws.api.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateProductRequest {

  @NotBlank private String name;

  private String unit;

  private Boolean active;
}
