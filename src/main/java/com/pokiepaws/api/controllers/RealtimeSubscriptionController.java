package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.realtime.NotificationSubscriptionResponse;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.services.VetService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RealtimeSubscriptionController {

  private final VetService vetService;

  @GetMapping("/api/admin/notifications/subscriptions")
  @PreAuthorize("hasRole('ADMIN')")
  public List<NotificationSubscriptionResponse> getAdminSubscriptions() {
    return subscriptions(
        "/topic/admin/activity", "/topic/warehouse/orders", "/topic/warehouse/stock");
  }

  @GetMapping("/api/vets/me/notifications/subscriptions")
  @PreAuthorize("hasRole('VET')")
  @Transactional(readOnly = true)
  public List<NotificationSubscriptionResponse> getVetSubscriptions(Authentication authentication) {
    Vet vet = vetService.getByEmail(authentication.getName());
    Long clinicId = vet.getClinic().getId();

    return subscriptions(
        "/topic/clinics/" + clinicId + "/visits",
        "/topic/clinics/" + clinicId + "/orders",
        "/topic/clinics/" + clinicId + "/stock",
        "/topic/clinics/" + clinicId + "/lab-orders");
  }

  @GetMapping("/api/warehouse-workers/me/notifications/subscriptions")
  @PreAuthorize("hasRole('WAREHOUSE')")
  public List<NotificationSubscriptionResponse> getWarehouseSubscriptions() {
    return subscriptions("/topic/warehouse/orders", "/topic/warehouse/stock");
  }

  private List<NotificationSubscriptionResponse> subscriptions(String... topics) {
    return Arrays.stream(topics).map(NotificationSubscriptionResponse::new).toList();
  }
}
