package com.pokiepaws.api.controllers;

import com.pokiepaws.api.config.OpenApiConfig;
import com.pokiepaws.api.dto.realtime.NotificationSubscriptionResponse;
import com.pokiepaws.api.services.StaffNotificationSubscriptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RequestMapping(value = "/api/vets/me/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@PreAuthorize("hasRole('VET')")
public class VetNotificationController {

  private final StaffNotificationSubscriptionService subscriptionService;

  @GetMapping("/subscriptions")
  public NotificationSubscriptionResponse subscriptions() {
    return subscriptionService.getCurrentVetSubscriptions();
  }
}
