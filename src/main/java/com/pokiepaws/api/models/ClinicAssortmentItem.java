package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "clinic_assortment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicAssortmentItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id", nullable = false)
  private Clinic clinic;

  @Column(nullable = false)
  private String name;

  private int amount;

  @Column(columnDefinition = "TEXT")
  private String description;

  private String category;

  @Builder.Default private String status = "PENDING";

  private String unit;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;
}
