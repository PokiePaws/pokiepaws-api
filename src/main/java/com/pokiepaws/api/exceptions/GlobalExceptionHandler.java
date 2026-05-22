package com.pokiepaws.api.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String VALIDATION_FAILED = "Validation failed";
  private static final String DATA_CONFLICT = "Data conflict";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));

    return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, fields);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation ->
                fields.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));

    return build(HttpStatus.BAD_REQUEST, VALIDATION_FAILED, request, fields);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
      HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, DATA_CONFLICT, request, null);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorResponse> handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String message = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
    return build(status, message, request, null);
  }

  private ResponseEntity<ApiErrorResponse> build(
      HttpStatus status, String message, HttpServletRequest request, Map<String, String> fields) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            fields == null || fields.isEmpty() ? null : fields);

    return ResponseEntity.status(status).body(response);
  }
}
