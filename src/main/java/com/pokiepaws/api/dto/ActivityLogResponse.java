package com.pokiepaws.api.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {

  private Long id;
  private String type;
  private String userEmail;
  private String detail;
  private String clinic;
  private LocalDateTime time;
}
