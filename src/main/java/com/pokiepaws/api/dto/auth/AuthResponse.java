package com.pokiepaws.api.dto.auth;

import lombok.Data;

@Data
public class AuthResponse {
  private String accessToken;
  private String refreshToken;
  private String email;
  private String role;
  private boolean mfaRequired;

  public static AuthResponse authenticated(
      String accessToken, String refreshToken, String email, String role) {
    AuthResponse response = new AuthResponse();
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);
    response.setEmail(email);
    response.setRole(role);
    response.setMfaRequired(false);
    return response;
  }

  public static AuthResponse mfaRequired(String email, String role) {
    AuthResponse response = new AuthResponse();
    response.setEmail(email);
    response.setRole(role);
    response.setMfaRequired(true);
    return response;
  }
}
