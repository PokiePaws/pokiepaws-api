package com.pokiepaws.api.dto.vet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VetResponse {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String phone;
  private String npwz;
  private String specialization;
  private String clinicName;
}
