package com.pokiepaws.api.dto;

import com.pokiepaws.api.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
  private Long id;
  private String email;
  private String role;
  private boolean active;
  private boolean emailVerified;

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getRole().name(),
        user.isActive(),
        user.isEmailVerified());
  }
}
