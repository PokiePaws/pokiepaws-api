package com.pokiepaws.api.dto.laborder;

import com.pokiepaws.api.models.LabOrderStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LabOrderStatusHistoryResponse {
  Long id;
  LabOrderStatus previousStatus;
  LabOrderStatus newStatus;
  String changedByEmail;
  LocalDateTime changedAt;
}
