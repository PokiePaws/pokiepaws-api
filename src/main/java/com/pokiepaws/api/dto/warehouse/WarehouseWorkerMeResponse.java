package com.pokiepaws.api.dto.warehouse;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WarehouseWorkerMeResponse {
    Long warehouseId;
    String warehouseName;
    String firstName;
    String lastName;
    String email;
}