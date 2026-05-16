package com.pokiepaws.api.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ClinicAssortmentItemRequest {

    @NotNull
    private Long clinicId;

    @NotBlank
    private String name;

    @Min(1)
    private int amount;

    private String description;
    private String category;
    private String unit;
    private LocalDate expiryDate;
}