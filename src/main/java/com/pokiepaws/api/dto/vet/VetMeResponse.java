package com.pokiepaws.api.dto.vet;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VetMeResponse {
  Long userId;
  String firstName;
  String lastName;
  String phone;
  String npwz;
  String specialization;
  Long clinicId;
  String clinicName;
}
