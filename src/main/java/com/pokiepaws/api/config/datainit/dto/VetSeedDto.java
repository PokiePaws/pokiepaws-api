package com.pokiepaws.api.config.datainit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VetSeedDto {
  private final String email;
  private final String password;
  private final String firstName;
  private final String lastName;
  private final String phone;
  private final String npwz;
  private final String specialization;
  private final String clinicName;
}
