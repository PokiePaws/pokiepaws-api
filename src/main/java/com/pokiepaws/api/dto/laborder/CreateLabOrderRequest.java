package com.pokiepaws.api.dto.laborder;

import com.pokiepaws.api.models.LabOrderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLabOrderRequest {
  @NotBlank private String testType;

  @NotNull private LabOrderPriority priority;

  private String clinicalReason;

  private Long visitId;
}
