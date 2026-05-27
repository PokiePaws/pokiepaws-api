package com.pokiepaws.api.dto.vet;

import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VetWorkingHoursResponse {
  Long id;
  Long vetUserId;
  DayOfWeek dayOfWeek;
  LocalTime startTime;
  LocalTime endTime;
  LocalTime breakStart;
  LocalTime breakEnd;
  boolean active;
}
