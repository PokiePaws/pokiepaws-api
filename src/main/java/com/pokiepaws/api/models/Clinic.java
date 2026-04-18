package com.pokiepaws.api.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clinics")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String city;

    @Column
    private String address;

    @Column
    private String phone;

    @Column
    private String email;

    @Column
    private String manager;

    @Column
    private String image;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Double rating;
}