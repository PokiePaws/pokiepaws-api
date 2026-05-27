package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "lab_order_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabOrderStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "lab_order_id", nullable = false)
  private LabOrder labOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "previous_status")
  private LabOrderStatus previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false)
  private LabOrderStatus newStatus;

  @Column(name = "changed_by_email")
  private String changedByEmail;

  @Column(name = "changed_at", nullable = false, updatable = false)
  private LocalDateTime changedAt;

  @PrePersist
  protected void onCreate() {
    this.changedAt = LocalDateTime.now();
  }
}
