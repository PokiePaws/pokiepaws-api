package com.pokiepaws.api.dto.realtime;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RealtimeNotification {
  RealtimeEventType type;
  Long entityId;
  String message;
  LocalDateTime createdAt;
  Map<String, Object> details;
}
