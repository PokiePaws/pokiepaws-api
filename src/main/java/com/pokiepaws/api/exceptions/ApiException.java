package com.pokiepaws.api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ApiException extends ResponseStatusException {

  private ApiException(HttpStatus status, String reason) {
    super(status, reason);
  }

  public static ApiException badRequest(String reason) {
    return new ApiException(HttpStatus.BAD_REQUEST, reason);
  }

  public static ApiException conflict(String reason) {
    return new ApiException(HttpStatus.CONFLICT, reason);
  }

  public static ApiException forbidden(String reason) {
    return new ApiException(HttpStatus.FORBIDDEN, reason);
  }

  public static ApiException notFound(String reason) {
    return new ApiException(HttpStatus.NOT_FOUND, reason);
  }

  public static ApiException unauthorized(String reason) {
    return new ApiException(HttpStatus.UNAUTHORIZED, reason);
  }
}
