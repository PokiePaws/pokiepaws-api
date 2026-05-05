package com.pokiepaws.api.controllers;

import com.pokiepaws.api.config.OpenApiConfig;
import com.pokiepaws.api.dto.ActivityLogResponse;
import com.pokiepaws.api.services.ActivityLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RequestMapping(value = "/api/admin/logs", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class ActivityLogController {

  private final ActivityLogService activityLogService;

  @GetMapping
  public ResponseEntity<List<ActivityLogResponse>> getAll(
      @RequestParam(required = false, defaultValue = "all") String type,
      @RequestParam(required = false, defaultValue = "100") int limit) {
    return ResponseEntity.ok(activityLogService.getAll(type, limit));
  }

  @GetMapping("/stats")
  public ResponseEntity<Map<String, Long>> stats() {
    return ResponseEntity.ok(Map.of("today", activityLogService.countToday()));
  }
}
