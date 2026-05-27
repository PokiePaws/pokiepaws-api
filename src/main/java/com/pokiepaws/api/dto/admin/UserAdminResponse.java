package com.pokiepaws.api.dto.admin;

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
public class UserAdminResponse {

  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String role;
  private Long clinicId;
  private String clinicName;
  private Long warehouseId;
  private String warehouseName;
  private boolean active;
  private boolean emailVerified;
  private String npwz;
  private String phone;
  private String specialization;
}
