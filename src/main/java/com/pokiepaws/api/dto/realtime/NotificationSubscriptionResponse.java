package com.pokiepaws.api.dto.realtime;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class NotificationSubscriptionResponse {
  String webSocketEndpoint;
  String userQueue;
  List<String> topics;
}
