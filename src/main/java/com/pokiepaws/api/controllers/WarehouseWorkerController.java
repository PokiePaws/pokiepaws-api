package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.warehouse.WarehouseWorkerMeResponse;
import com.pokiepaws.api.models.WarehouseWorker;
import com.pokiepaws.api.repositories.WarehouseWorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/warehouse-workers")
@RequiredArgsConstructor
public class WarehouseWorkerController {

    private final WarehouseWorkerRepository warehouseWorkerRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('WAREHOUSE')")
    public WarehouseWorkerMeResponse getMe(Authentication authentication) {
        String email = authentication.getName();
        WarehouseWorker worker = warehouseWorkerRepository.findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse worker not found"));

        return WarehouseWorkerMeResponse.builder()
                .warehouseId(worker.getWarehouse().getId())
                .warehouseName(worker.getWarehouse().getWarehouseName())
                .firstName(worker.getFirstName())
                .lastName(worker.getLastName())
                .email(email)
                .build();
    }
}