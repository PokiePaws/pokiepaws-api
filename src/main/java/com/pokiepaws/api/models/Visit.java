package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Visit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "animal_id", nullable = false)
  private Animal animal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vet_user_id")
  private Vet vet;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id", nullable = false)
  private Clinic clinic;

  @Column(name = "starts_at", nullable = false)
  private LocalDateTime startsAt;

  @Column(name = "ends_at", nullable = false)
  private LocalDateTime endsAt;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "disease")
  private String disease;

  @Column(name = "diagnosis", columnDefinition = "TEXT")
  private String diagnosis;

  @Column(name = "recommendations", columnDefinition = "TEXT")
  private String recommendations;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private VisitStatus status = VisitStatus.SCHEDULED;

  @Builder.Default
  @Column(name = "used", nullable = false)
  private boolean used = false;

  @Builder.Default
  @Column(name = "reminder_24h_sent", nullable = false)
  private boolean reminder24hSent = false;

  @Builder.Default
  @Column(name = "reminder_1h_sent", nullable = false)
  private boolean reminder1hSent = false;
}
