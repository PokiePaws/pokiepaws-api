package com.pokiepaws.api.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "warehouse_name", nullable = false)
  private String warehouseName;

  @Column(name = "regon")
  private String regon;

  @Column(name = "nip")
  private String nip;

  @Column(name = "street", nullable = false)
  private String street;

  @Column(name = "house_number", nullable = false)
  private String houseNumber;

  @Column(name = "apartment_number")
  private String apartmentNumber;

  @Column(name = "postal_code", nullable = false)
  private String postalCode;

  @Column(name = "city", nullable = false)
  private String city;

  @Column(name = "country", nullable = false)
  private String country;

  @Column(name = "working_hours")
  private String workingHours;

  @Column(name = "phone")
  private String phone;

  @Column(name = "email")
  private String email;

  @Builder.Default
  @Column(nullable = false)
  private boolean active = true;
}
