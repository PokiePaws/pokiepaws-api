package com.pokiepaws.api.dto.clinic;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClinicResponse {
  Long id;
  String clinicName;
  String street;
  String houseNumber;
  String apartmentNumber;
  String postalCode;
  String city;
  String country;
  String phone;
  String email;
  String workingHours;
}
