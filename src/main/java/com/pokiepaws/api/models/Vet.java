package com.pokiepaws.api.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vets")
@Builder
public class Vet {
  @Id
<<<<<<< HEAD

=======
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
>>>>>>> 4e343b6 (Merge fix)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "clinic_id")
  private Clinic clinic;

<<<<<<< HEAD

}
=======
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
}
>>>>>>> 4e343b6 (Merge fix)
