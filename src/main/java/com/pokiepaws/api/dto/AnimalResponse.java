package com.pokiepaws.api.dto;

import com.pokiepaws.api.models.Animal;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnimalResponse {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private Animal.Gender gender;
    private String color;
    private String microchipNumber;
    private Double weight;
    private LocalDate birthDate;
    private String notes;
}