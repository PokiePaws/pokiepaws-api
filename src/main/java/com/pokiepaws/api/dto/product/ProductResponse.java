package com.pokiepaws.api.dto.product;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductResponse {
  Long id;
  String name;
  String unit;
  boolean active;
}
