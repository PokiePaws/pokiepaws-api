package com.pokiepaws.api.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class WarehouseStockItemRequest {
    @NotNull
    private Long warehouseId;
    @NotBlank
    private String name;
    private String assortmentDescription;
    private Double price;
    private String unit;
    private String category;
    private int amount;
    private LocalDate expiryDate;
    private String status;
}