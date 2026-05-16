package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.mobile.OwnerDeviceTokenResponse;
import com.pokiepaws.api.dto.mobile.RegisterOwnerDeviceTokenRequest;
import com.pokiepaws.api.services.OwnerDeviceTokenService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/me/device-tokens")
@PreAuthorize("hasRole('OWNER')")
public class OwnerDeviceTokenController {

  private final OwnerDeviceTokenService ownerDeviceTokenService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OwnerDeviceTokenResponse register(
      @Valid @RequestBody RegisterOwnerDeviceTokenRequest request) {
    return ownerDeviceTokenService.register(request);
  }

  @GetMapping
  public List<OwnerDeviceTokenResponse> getCurrentOwnerDeviceTokens() {
    return ownerDeviceTokenService.getCurrentOwnerDeviceTokens();
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@RequestParam String token) {
    ownerDeviceTokenService.deleteCurrentOwnerDeviceToken(token);
  }
}
