package com.pokiepaws.api.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path,
                               Map<String, String> fields) {

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
}
