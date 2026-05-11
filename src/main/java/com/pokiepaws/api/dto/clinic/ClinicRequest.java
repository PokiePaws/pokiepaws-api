package com.pokiepaws.api.dto.clinic;

import jakarta.validation.constraints.NotBlank;
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
public class ClinicRequest {

  @NotBlank private String name;

  @NotBlank private String address;

  @NotBlank private String hours;

  @Builder.Default private boolean active = true;
}
