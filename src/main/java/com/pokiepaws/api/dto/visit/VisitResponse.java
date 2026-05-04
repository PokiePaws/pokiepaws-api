package com.pokiepaws.api.dto.visit;

import com.pokiepaws.api.models.VisitStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VisitResponse {
  Long id;
  Long animalId;
  Long clinicId;
  Long vetUserId;
  LocalDateTime startsAt;
  LocalDateTime endsAt;
  String description;
  String disease;
  String diagnosis;
  String recommendations;
  VisitStatus status;
  boolean used;
}
