package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.ownersettings.OwnerProfileSettingsResponse;
import com.pokiepaws.api.dto.ownersettings.SettingsUpdateOwnerAddressRequest;
import com.pokiepaws.api.dto.ownersettings.SettingsUpdateOwnerPasswordRequest;
import com.pokiepaws.api.dto.ownersettings.SettingsUpdateOwnerPhoneRequest;
import com.pokiepaws.api.services.OwnerProfileSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owners/me")
@PreAuthorize("hasRole('OWNER')")
public class OwnerProfileSettingsController {

  private final OwnerProfileSettingsService ownerProfileService;

  @GetMapping
  public OwnerProfileSettingsResponse getCurrentOwnerProfile() {
    return ownerProfileService.getCurrentOwnerProfile();
  }

  @PatchMapping("/phone")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updatePhone(@Valid @RequestBody SettingsUpdateOwnerPhoneRequest request) {
    ownerProfileService.updatePhone(request);
  }

  @PatchMapping("/address")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateAddress(@Valid @RequestBody SettingsUpdateOwnerAddressRequest request) {
    ownerProfileService.updateAddress(request);
  }

  @PatchMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updatePassword(@Valid @RequestBody SettingsUpdateOwnerPasswordRequest request) {
    ownerProfileService.updatePassword(request);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCurrentOwnerAccount() {
    ownerProfileService.deleteCurrentOwnerAccount();
  }
}
