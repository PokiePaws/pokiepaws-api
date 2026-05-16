package com.pokiepaws.api.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStatusRequest {
  @NotBlank private String status;
}
