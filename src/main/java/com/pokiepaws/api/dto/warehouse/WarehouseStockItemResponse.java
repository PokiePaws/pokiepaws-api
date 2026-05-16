package com.pokiepaws.api.dto.warehouse;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WarehouseStockItemResponse {
    Long id;
    Long warehouseId;
    String name;
    String assortmentDescription;
    Double price;
    String unit;
    String category;
    int amount;
    LocalDate expiryDate;
    String status;
}