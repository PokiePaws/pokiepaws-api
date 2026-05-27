package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "lab_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "animal_id", nullable = false)
  private Animal animal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "visit_id")
  private Visit visit;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vet_user_id", nullable = false)
  private Vet vet;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id", nullable = false)
  private Clinic clinic;

  @Column(name = "test_type", nullable = false)
  private String testType;

  @Column(name = "clinical_reason", columnDefinition = "TEXT")
  private String clinicalReason;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  @Builder.Default
  private LabOrderPriority priority = LabOrderPriority.NORMAL;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Builder.Default
  private LabOrderStatus status = LabOrderStatus.PENDING;

  @Column(name = "warehouse_order_id")
  private Long warehouseOrderId;

  @Column(name = "ordered_at", nullable = false, updatable = false)
  private LocalDateTime orderedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @PrePersist
  protected void onCreate() {
    this.orderedAt = LocalDateTime.now();
  }
}
