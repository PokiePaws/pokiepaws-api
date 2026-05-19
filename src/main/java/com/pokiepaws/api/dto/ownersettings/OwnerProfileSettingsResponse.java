package com.pokiepaws.api.dto.ownersettings;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OwnerProfileSettingsResponse {
  Long userId;
  String email;
  String firstName;
  String lastName;
  String phoneNumber;
  String street;
  String houseNumber;
  String apartmentNumber;
  String postalCode;
  String city;
  String country;
}
