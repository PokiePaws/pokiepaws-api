package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.auth.ActivityLogResponse;
import com.pokiepaws.api.models.ActivityLog;
import com.pokiepaws.api.models.ActivityLog.LogType;
import com.pokiepaws.api.repositories.ActivityLogRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

  private final ActivityLogRepository activityLogRepository;
  private final Clock clock;
  private final RealtimeNotificationService realtimeNotificationService;

  @Transactional
  public void log(LogType type, String detail, String clinic) {
    ActivityLog saved =
        activityLogRepository.save(
            ActivityLog.builder()
                .type(type)
                .userEmail(currentUserEmail())
                .detail(detail)
                .clinic(clinic)
                .time(LocalDateTime.now(clock))
                .build());
    realtimeNotificationService.publishActivityLogCreated(toDto(saved));
  }

  @Transactional
  public void logFor(String userEmail, LogType type, String detail, String clinic) {
    ActivityLog saved =
        activityLogRepository.save(
            ActivityLog.builder()
                .type(type)
                .userEmail(userEmail)
                .detail(detail)
                .clinic(clinic)
                .time(LocalDateTime.now(clock))
                .build());
    realtimeNotificationService.publishActivityLogCreated(toDto(saved));
  }

  @Transactional(readOnly = true)
  public List<ActivityLogResponse> getAll(String typeFilter, int limit) {
    Pageable pageable = PageRequest.of(0, limit);

    List<ActivityLog> logs;
    if (typeFilter == null || typeFilter.isBlank() || "all".equalsIgnoreCase(typeFilter)) {
      logs = activityLogRepository.findAllByOrderByTimeDesc(pageable);
    } else {
      try {
        logs =
            activityLogRepository.findByTypeOrderByTimeDesc(LogType.valueOf(typeFilter), pageable);
      } catch (IllegalArgumentException ex) {
        logs = List.of();
      }
    }

    return logs.stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public long countToday() {
    return activityLogRepository.countByTimeAfter(LocalDate.now(clock).atStartOfDay());
  }

  @Transactional(readOnly = true)
  public Map<String, Long> getStats() {
    Map<String, Long> stats = new LinkedHashMap<>();
    stats.put("today", countToday());
    for (ActivityLog.LogType type : ActivityLog.LogType.values()) {
      stats.put(type.name(), activityLogRepository.countByType(type));
    }
    return stats;
  }

  private ActivityLogResponse toDto(ActivityLog l) {
    return ActivityLogResponse.builder()
        .id(l.getId())
        .type(l.getType().name())
        .userEmail(l.getUserEmail())
        .detail(l.getDetail())
        .clinic(l.getClinic())
        .time(l.getTime())
        .build();
  }

  private String currentUserEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth != null ? auth.getName() : "system";
  }
}
