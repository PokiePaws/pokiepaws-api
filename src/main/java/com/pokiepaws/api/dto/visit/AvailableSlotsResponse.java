package com.pokiepaws.api.dto.visit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AvailableSlotsResponse {
  Long clinicId;
  Long vetUserId;
  LocalDate date;
  int slotMinutes;
  LocalDateTime workdayStart;
  LocalDateTime workdayEnd;
  List<LocalDateTime> availableStarts;
}
