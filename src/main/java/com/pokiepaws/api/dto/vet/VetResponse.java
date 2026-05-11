package com.pokiepaws.api.dto.vet;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VetResponse {
  Long id;
  String email;
  String firstName;
  String lastName;
  String phone;
  String npwz;
  String specialization;
  String clinicName;
}
