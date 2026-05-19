package com.pokiepaws.api.dto.ownersettings;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettingsUpdateOwnerPasswordRequest {
  @NotBlank private String currentPassword;

  @NotBlank private String newPassword;
}
