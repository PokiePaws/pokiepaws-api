package com.pokiepaws.api.dto.ownersettings;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettingsUpdateOwnerPhoneRequest {
  @NotBlank private String phoneNumber;
}
