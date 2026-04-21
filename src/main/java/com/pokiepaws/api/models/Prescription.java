package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(
    name = "prescriptions",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_prescriptions_visit",
          columnNames = {"visit_id"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "visit_id", nullable = false)
  private Visit visit;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "vet_user_id", nullable = false)
  private Vet vet;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id", nullable = false)
  private Clinic clinic;

  @Column(name = "recommendation_date")
  private LocalDate recommendationDate;

  @Column(name = "creation_date", nullable = false)
  private LocalDate creationDate;

  @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PrescriptionItem> items = new ArrayList<>();

  public void addItem(PrescriptionItem item) {
    items.add(item);
    item.setPrescription(this);
  }

  public void removeItem(PrescriptionItem item) {
    items.remove(item);
    item.setPrescription(null);
  }

}
