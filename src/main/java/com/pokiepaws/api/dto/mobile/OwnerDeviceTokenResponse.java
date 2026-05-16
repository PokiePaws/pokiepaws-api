package com.pokiepaws.api.dto.mobile;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OwnerDeviceTokenResponse {
  Long id;
  String platform;
  LocalDateTime createdAt;
  LocalDateTime lastUsedAt;
}
