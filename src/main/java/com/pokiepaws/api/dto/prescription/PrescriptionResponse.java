package com.pokiepaws.api.dto.prescription;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PrescriptionResponse {
  Long id;
  Long visitId;
  Long vetUserId;
  Long clinicId;
  LocalDate recommendationDate;
  LocalDate creationDate;
  List<PrescriptionItemResponse> items;
}
