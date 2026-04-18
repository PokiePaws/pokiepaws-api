package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Warehouse;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
  Optional<Warehouse> findByWarehouseNameIgnoreCase(String warehouseName);
}
