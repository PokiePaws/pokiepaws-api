package com.pokiepaws.api.exceptions;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;

@Getter
public class ApiErrorResponse {

  private final LocalDateTime timestamp;
  private final int status;
  private final String error;
  private final String message;
  private final String path;
  private final Map<String, String> fields;

  public ApiErrorResponse(
      LocalDateTime timestamp,
      int status,
      String error,
      String message,
      String path,
      Map<String, String> fields) {
    this.timestamp = timestamp;
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.fields = fields == null ? null : Map.copyOf(fields);
  }

  public Map<String, String> getFields() {
    return fields == null ? null : Map.copyOf(fields);
  }
}
