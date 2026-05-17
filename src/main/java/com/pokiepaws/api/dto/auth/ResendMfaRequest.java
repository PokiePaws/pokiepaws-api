package com.pokiepaws.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendMfaRequest {

  @Email @NotBlank private String email;
}
