package com.pokiepaws.api.dto.laborder;

import com.pokiepaws.api.models.LabOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLabOrderStatusRequest {
  @NotNull private LabOrderStatus status;
}
