package com.pokiepaws.api.dto.vet;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Data;

@Data
public class VetWorkingHoursRequest {
  @NotNull private DayOfWeek dayOfWeek;
  @NotNull private LocalTime startTime;
  @NotNull private LocalTime endTime;
  private LocalTime breakStart;
  private LocalTime breakEnd;
  private boolean active = true;
}
