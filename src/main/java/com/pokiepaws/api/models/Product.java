package com.pokiepaws.api.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "products",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_products_name",
          columnNames = {"name"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "unit")
  private String unit;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;
}
