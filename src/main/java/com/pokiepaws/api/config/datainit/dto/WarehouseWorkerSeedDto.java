package com.pokiepaws.api.config.datainit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseWorkerSeedDto {
  private final String email;
  private final String password;
  private final String firstName;
  private final String lastName;
  private final String phoneNumber;
  private final String warehouseName;
}
