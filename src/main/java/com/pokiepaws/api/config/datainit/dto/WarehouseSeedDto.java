package com.pokiepaws.api.config.datainit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseSeedDto {
  private final String name;
  private final String email;
  private final String regon;
  private final String nip;
  private final String phone;
  private final String street;
  private final String houseNumber;
  private final String apartmentNumber;
  private final String postalCode;
  private final String city;
  private final String country;
  private final String workingHours;
}
