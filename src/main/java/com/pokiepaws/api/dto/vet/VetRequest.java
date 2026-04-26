package com.pokiepaws.api.dto.vet;

import lombok.Data;

@Data
public class VetRequest {
  private String firstName;
  private String lastName;
  private String phone;
  private String npwz;
  private String specialization;
  private Long clinicId;
}
