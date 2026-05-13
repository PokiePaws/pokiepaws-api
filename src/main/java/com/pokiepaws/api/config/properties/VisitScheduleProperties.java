package com.pokiepaws.api.config.properties;

import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.visits.schedule")
public class VisitScheduleProperties {
  private int slotMinutes = 30;
  private LocalTime workStart = LocalTime.of(9, 0);
  private LocalTime workEnd = LocalTime.of(17, 0);
}
