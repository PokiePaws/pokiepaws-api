package com.pokiepaws.api.dto.mobile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOwnerDeviceTokenRequest {
  @NotBlank
  @Size(max = 512)
  private String token;

  @Size(max = 32)
  private String platform = "ANDROID";
}
