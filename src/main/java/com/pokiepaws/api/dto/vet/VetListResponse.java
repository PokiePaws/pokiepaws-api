package com.pokiepaws.api.dto.vet;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VetListResponse {
  Long userId;
  String firstName;
  String lastName;
  String npwz;
  String specialization;
}
