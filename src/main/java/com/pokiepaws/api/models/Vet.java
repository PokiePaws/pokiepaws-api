package com.pokiepaws.api.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "vets")
@Builder
@SQLDelete(sql = "UPDATE vets SET active = false WHERE user_id = ?")
@SQLRestriction("active = true")
public class Vet {
  @Id

  @Column(name = "user_id")
  private Long userId;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
  
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id")
  private Clinic clinic;

}
  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "phone")
  private String phone;

  @Column(name = "npwz", nullable = false, unique = true)
  private String npwz;

  @Column(name = "specialization")
  private String specialization;

  @Builder.Default
  @Column(name = "active", nullable = false)
  private boolean active = true;
}
