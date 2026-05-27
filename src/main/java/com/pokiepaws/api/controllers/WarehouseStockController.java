package com.pokiepaws.api.controllers;

import com.pokiepaws.api.dto.warehouse.WarehouseStockItemRequest;
import com.pokiepaws.api.dto.warehouse.WarehouseStockItemResponse;
import com.pokiepaws.api.services.WarehouseStockService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse/stock")
@RequiredArgsConstructor
public class WarehouseStockController {

  private final WarehouseStockService service;

  @GetMapping
  @PreAuthorize("hasAnyRole('WAREHOUSE','VET')")
  public List<WarehouseStockItemResponse> getAll() {
    return service.getAll();
  }

  @GetMapping("/warehouse/{warehouseId}")
  @PreAuthorize("hasAnyRole('WAREHOUSE','VET')")
  public List<WarehouseStockItemResponse> getByWarehouse(@PathVariable Long warehouseId) {
    return service.getByWarehouse(warehouseId);
  }

  @GetMapping("/low-stock")
  @PreAuthorize("hasAnyRole('WAREHOUSE','VET')")
  public List<WarehouseStockItemResponse> getLowStock(
      @RequestParam(defaultValue = "10") int threshold) {
    return service.getLowStock(threshold);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('WAREHOUSE','VET')")
  public WarehouseStockItemResponse getById(@PathVariable Long id) {
    return service.getById(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('WAREHOUSE')")
  public WarehouseStockItemResponse create(@Valid @RequestBody WarehouseStockItemRequest request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('WAREHOUSE')")
  public WarehouseStockItemResponse update(
      @PathVariable Long id, @Valid @RequestBody WarehouseStockItemRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('WAREHOUSE')")
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
