package com.pokiepaws.api.dto.vet;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateVetVisitRequest {

  @NotNull private Long animalId;

  @NotNull private LocalDateTime startsAt;

  private String description;
}
