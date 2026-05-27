package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(
    name = "vet_working_hours",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vet_user_id", "day_of_week"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VetWorkingHours {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vet_user_id", nullable = false)
  private Vet vet;

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_week", nullable = false)
  private DayOfWeek dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "break_start")
  private LocalTime breakStart;

  @Column(name = "break_end")
  private LocalTime breakEnd;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;
}
