package com.pokiepaws.api.dto.vet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VetResponse {
  Long id;
  Long userId;
  String email;
  String firstName;
  String lastName;
  String phone;
  String npwz;
  String specialization;
  Long clinicId;
  String clinicName;
}
