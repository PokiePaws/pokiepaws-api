package com.pokiepaws.api.dto.visit;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateVisitRequest {
  @NotNull private Long animalId;
  @NotNull private Long clinicId;
  @NotNull private Long vetUserId;

  @NotNull private LocalDateTime startsAt;
  private String description;
}
