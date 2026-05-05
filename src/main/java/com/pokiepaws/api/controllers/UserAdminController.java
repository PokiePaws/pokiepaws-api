package com.pokiepaws.api.controllers;

import com.pokiepaws.api.config.OpenApiConfig;
import com.pokiepaws.api.dto.UserAdminRequest;
import com.pokiepaws.api.dto.UserAdminResponse;
import com.pokiepaws.api.services.UserAdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RequestMapping(value = "/api/admin/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class UserAdminController {

  private final UserAdminService userAdminService;

  @GetMapping
  public ResponseEntity<List<UserAdminResponse>> getAll() {
    return ResponseEntity.ok(userAdminService.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserAdminResponse> getOne(@PathVariable Long id) {
    return ResponseEntity.ok(userAdminService.findById(id));
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserAdminResponse> create(@Valid @RequestBody UserAdminRequest request) {
    UserAdminResponse created = userAdminService.create(request);
    return ResponseEntity.created(URI.create("/api/admin/users/" + created.getId())).body(created);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<UserAdminResponse> update(
      @PathVariable Long id, @Valid @RequestBody UserAdminRequest request) {
    return ResponseEntity.ok(userAdminService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userAdminService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
