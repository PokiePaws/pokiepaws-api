package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.realtime.NotificationSubscriptionResponse;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.VetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StaffNotificationSubscriptionService {

  private static final String WEB_SOCKET_ENDPOINT = "/ws-native";
  private static final String USER_NOTIFICATIONS_QUEUE = "/user/queue/notifications";
  private static final String ADMIN_ACTIVITY_TOPIC = "/topic/admin/activity";

  private final VetRepository vetRepository;

  public NotificationSubscriptionResponse getAdminSubscriptions() {
    return NotificationSubscriptionResponse.builder()
        .webSocketEndpoint(WEB_SOCKET_ENDPOINT)
        .userQueue(USER_NOTIFICATIONS_QUEUE)
        .topics(List.of(ADMIN_ACTIVITY_TOPIC))
        .build();
  }

  @Transactional(readOnly = true)
  public NotificationSubscriptionResponse getCurrentVetSubscriptions() {
    Vet vet =
        vetRepository
            .findByUserEmail(currentUserEmail())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Vet profile not found for current user"));

    Long clinicId = vet.getClinic() == null ? null : vet.getClinic().getId();
    List<String> topics =
        clinicId == null
            ? List.of()
            : List.of(
                "/topic/clinics/" + clinicId + "/visits",
                "/topic/clinics/" + clinicId + "/stock");

    return NotificationSubscriptionResponse.builder()
        .webSocketEndpoint(WEB_SOCKET_ENDPOINT)
        .userQueue(USER_NOTIFICATIONS_QUEUE)
        .topics(topics)
        .build();
  }

  private String currentUserEmail() {
    return SecurityContextHolder.getContext().getAuthentication().getName();
  }
}
