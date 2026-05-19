package com.pokiepaws.api.dto.laborder;

import com.pokiepaws.api.models.LabOrderPriority;
import com.pokiepaws.api.models.LabOrderStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LabOrderResponse {
  Long id;
  Long animalId;
  String animalName;
  String animalSpecies;
  Long visitId;
  Long vetUserId;
  String vetFirstName;
  String vetLastName;
  Long clinicId;
  String testType;
  String clinicalReason;
  LabOrderPriority priority;
  LabOrderStatus status;
  LocalDateTime orderedAt;
  LocalDateTime completedAt;
}
