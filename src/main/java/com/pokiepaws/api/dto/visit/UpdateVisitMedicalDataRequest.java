package com.pokiepaws.api.dto.visit;

import lombok.Data;

@Data
public class UpdateVisitMedicalDataRequest {
  private String disease;
  private String diagnosis;
  private String recommendations;
}
