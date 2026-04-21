package com.pokiepaws.api.dto.prescription;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PrescriptionItemResponse {
  Long id;
  Long productId;
  String productName;
  int quantityPackages;
  String dosage;
  String treatmentTime;
}
