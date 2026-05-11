package com.pokiepaws.api.config.datainit.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductSeedDto {
  private final String name;
  private final String unit;
  private final boolean active;
}
