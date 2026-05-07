package com.pokiepaws.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicResponse {

  private Long id;
  private String clinicName;
  private String regon;
  private String nip;

  private String street;
  private String houseNumber;
  private String apartmentNumber;
  private String postalCode;
  private String city;
  private String country;

  /** Sklejony adres w jednej linii — wygodne dla frontendu. */
  private String displayAddress;

  private String workingHours;
  private String phone;
  private String email;
  private boolean active;

  private String adminName;
}
