package com.pokiepaws.api.dto.visit;

import java.time.LocalDate;
import lombok.Data;

@Data
public class UpdateVisitMedicalDataRequest {
  private String disease;
  private String diagnosis;
  private String recommendations;
  private boolean rabiesVaccinationPerformed;
  private LocalDate rabiesVaccinationDate;
}
