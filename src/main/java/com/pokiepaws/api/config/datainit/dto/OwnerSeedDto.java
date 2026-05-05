package com.pokiepaws.api.config.datainit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OwnerSeedDto {
  private final String email;
  private final String password;
  private final String firstName;
  private final String lastName;
  private final String phoneNumber;
  private final String street;
  private final String houseNumber;
  private final String apartmentNumber;
  private final String postalCode;
  private final String city;
  private final String country;
}
