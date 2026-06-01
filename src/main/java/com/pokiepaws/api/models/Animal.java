package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "animals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Animal {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String species;

  @Column private String breed;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Gender gender;

  @Column private String color;

  @Column(unique = true)
  private String microchipNumber;

  @Column private Double weight;

  @Column private LocalDate birthDate;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "rabies_vaccination_date")
  private LocalDate rabiesVaccinationDate;

  @Builder.Default
  @Column(name = "rabies_vaccination_reminder_sent", nullable = false)
  private boolean rabiesVaccinationReminderSent = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_user_id", nullable = false)
  private Owner owner;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public enum Gender {
    MALE,
    FEMALE,
    HERMAPHRODITE
  }
}
