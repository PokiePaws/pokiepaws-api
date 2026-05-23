package com.pokiepaws.api.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminRequest {

  @NotBlank private String firstName;

  @NotBlank private String lastName;

  @NotBlank @Email private String email;

  private String password;

  @NotNull private String role;

  private Long clinicId;

  private Long warehouseId;

  @Builder.Default private boolean active = true;

  private String npwz;

  private String phone;

  private String specialization;
}
