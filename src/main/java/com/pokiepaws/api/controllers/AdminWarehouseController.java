package com.pokiepaws.api.controllers;

import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.repositories.WarehouseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/warehouses")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWarehouseController {

  private final WarehouseRepository warehouseRepository;

  @GetMapping
  public List<Warehouse> getAll() {
    return warehouseRepository.findAll();
  }
}
