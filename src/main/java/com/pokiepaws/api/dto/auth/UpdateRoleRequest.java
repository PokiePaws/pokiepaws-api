package com.pokiepaws.api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotBlank
    private String role;
}