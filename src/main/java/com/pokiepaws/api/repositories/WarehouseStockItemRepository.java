package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.WarehouseStockItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseStockItemRepository extends JpaRepository<WarehouseStockItem, Long> {
    List<WarehouseStockItem> findAllByWarehouseId(Long warehouseId);

    List<WarehouseStockItem> findAllByAmountLessThanEqual(int threshold);

    boolean existsByNameIgnoreCaseAndWarehouse_Id(String name, Long warehouseId);

    Optional<WarehouseStockItem> findByNameIgnoreCase(String name);
}