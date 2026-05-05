package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.UpdateRoleRequest;
import com.pokiepaws.api.dto.UserResponse;
import com.pokiepaws.api.services.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public List<UserResponse> getAll() {
    return adminUserService.getAllUsers();
  }

  @PatchMapping("/{id}/active")
  public UserResponse toggleActive(@PathVariable Long id) {
    return adminUserService.toggleActive(id);
  }

  @PatchMapping("/{id}/role")
  public UserResponse updateRole(
      @PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
    return adminUserService.updateRole(id, request.getRole());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    adminUserService.deleteUser(id);
    return ResponseEntity.noContent().build();
  }
}
