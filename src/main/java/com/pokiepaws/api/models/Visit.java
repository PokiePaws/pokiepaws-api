package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDate;
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

  @Column(name = "visit_date", nullable = false)
  private LocalDate visitDate;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "disease")
  private String disease;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private VisitStatus status = VisitStatus.SCHEDULED;

  @Builder.Default
  @Column(name = "used", nullable = false)
  private boolean used = false;
}
