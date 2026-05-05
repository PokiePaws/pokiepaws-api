package com.pokiepaws.api.dto;

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

  /** Wymagane przy tworzeniu, opcjonalne przy edycji. */
  private String password;

  @NotNull private String role;

  /** ID kliniki (opcjonalne dla niektórych ról). */
  private Long clinicId;

  @Builder.Default private boolean active = true;

  /** Numer Prawa Wykonywania Zawodu — wymagane dla VET. */
  private String npwz;

  private String phone;

  private String specialization;
}
