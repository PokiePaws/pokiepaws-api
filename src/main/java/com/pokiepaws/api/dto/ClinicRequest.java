package com.pokiepaws.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class ClinicRequest {

  @NotBlank private String clinicName;

  @NotBlank private String regon;

  private String nip;

  @NotBlank private String street;

  @NotBlank private String houseNumber;

  private String apartmentNumber;

  @NotBlank private String postalCode;

  @NotBlank private String city;

  @NotBlank private String country;

  private String workingHours;

  private String phone;

  @Email private String email;

  @Builder.Default private boolean active = true;
}
