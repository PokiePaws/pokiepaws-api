package com.pokiepaws.api.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "prescription_id", nullable = false)
  private Prescription prescription;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "stock_item_id", nullable = false)
  private WarehouseStockItem stockItem;

  @Column(name = "quantity_packages", nullable = false)
  private int quantityPackages;

  @Column(name = "dosage")
  private String dosage;

  @Column(name = "treatment_time")
  private String treatmentTime;
}
