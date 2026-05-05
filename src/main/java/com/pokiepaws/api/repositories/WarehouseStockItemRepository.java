package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.WarehouseStockItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseStockItemRepository extends JpaRepository<WarehouseStockItem, Long> {
  Optional<WarehouseStockItem> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}
