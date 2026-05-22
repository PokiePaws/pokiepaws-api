package com.pokiepaws.api.dto.prescription;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class CreatePrescriptionRequest {
  private LocalDate recommendationDate;

  @Valid @NotEmpty private List<PrescriptionItemRequest> items;
}
