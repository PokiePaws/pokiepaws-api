package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.mobile.OwnerDeviceTokenResponse;
import com.pokiepaws.api.dto.mobile.RegisterOwnerDeviceTokenRequest;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.Owner;
import com.pokiepaws.api.models.OwnerDeviceToken;
import com.pokiepaws.api.repositories.OwnerDeviceTokenRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerDeviceTokenService {

  private final OwnerRepository ownerRepository;
  private final OwnerDeviceTokenRepository ownerDeviceTokenRepository;
  private final Clock clock;

  @Transactional
  public OwnerDeviceTokenResponse register(RegisterOwnerDeviceTokenRequest request) {
    Owner owner = getCurrentOwner();
    LocalDateTime now = LocalDateTime.now(clock);

    OwnerDeviceToken deviceToken =
        ownerDeviceTokenRepository
            .findByToken(request.getToken())
            .orElseGet(() -> OwnerDeviceToken.builder().token(request.getToken()).build());

    deviceToken.setOwner(owner);
    deviceToken.setPlatform(normalizePlatform(request.getPlatform()));
    deviceToken.setLastUsedAt(now);

    return toResponse(ownerDeviceTokenRepository.save(deviceToken));
  }

  @Transactional(readOnly = true)
  public List<OwnerDeviceTokenResponse> getCurrentOwnerDeviceTokens() {
    Owner owner = getCurrentOwner();
    return ownerDeviceTokenRepository.findAllByOwnerUserId(owner.getUserId()).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public void deleteCurrentOwnerDeviceToken(String token) {
    Owner owner = getCurrentOwner();
    ownerDeviceTokenRepository.deleteByOwnerUserIdAndToken(owner.getUserId(), token);
  }

  private Owner getCurrentOwner() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return ownerRepository
        .findByUserEmail(email)
        .orElseThrow(() -> ApiException.forbidden(ApiErrorMessage.OWNER_PROFILE_NOT_FOUND));
  }

  private String normalizePlatform(String platform) {
    if (platform == null || platform.isBlank()) {
      return "ANDROID";
    }
    return platform.trim().toUpperCase(java.util.Locale.ROOT);
  }

  private OwnerDeviceTokenResponse toResponse(OwnerDeviceToken deviceToken) {
    return OwnerDeviceTokenResponse.builder()
        .id(deviceToken.getId())
        .platform(deviceToken.getPlatform())
        .createdAt(deviceToken.getCreatedAt())
        .lastUsedAt(deviceToken.getLastUsedAt())
        .build();
  }
}
