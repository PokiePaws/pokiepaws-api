package com.pokiepaws.api.dto.warehouse;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClinicAssortmentItemResponse {
    Long id;
    Long clinicId;
    String clinicName;
    String name;
    int amount;
    String description;
    String category;
    String status;
    String unit;
    LocalDate expiryDate;
}