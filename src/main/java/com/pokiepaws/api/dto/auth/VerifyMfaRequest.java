package com.pokiepaws.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyMfaRequest {

  @NotBlank private String token;
}
