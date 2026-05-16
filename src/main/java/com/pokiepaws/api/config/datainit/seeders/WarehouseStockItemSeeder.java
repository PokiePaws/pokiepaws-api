package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.models.WarehouseStockItem;
import com.pokiepaws.api.repositories.WarehouseRepository;
import com.pokiepaws.api.repositories.WarehouseStockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"})
public class WarehouseStockItemSeeder implements Seeder {

  private final WarehouseRepository warehouseRepository;
  private final WarehouseStockItemRepository warehouseStockItemRepository;

  @Override
  public int order() {
    return 55;
  }

  @Override
  @Transactional
  public void seed() {
    if (warehouseStockItemRepository.count() > 0) {
      log.info("Warehouse stock items already seeded.");
      return;
    }

    Warehouse warehouse =
        warehouseRepository
            .findByWarehouseNameIgnoreCase(WarehousesSeeder.WAREHOUSE_MAIN)
            .orElse(null);

    if (warehouse == null) {
      log.warn("Warehouse not found, skipping warehouse stock seeder.");
      return;
    }

    warehouseStockItemRepository.save(
        WarehouseStockItem.builder()
            .warehouse(warehouse)
            .name("Amoksycylina 500mg")
            .category("Antybiotyki")
            .unit("opakowanie")
            .price(24.99)
            .amount(150)
            .status("AVAILABLE")
            .build());

    warehouseStockItemRepository.save(
        WarehouseStockItem.builder()
            .warehouse(warehouse)
            .name("Karprogen 50mg")
            .category("Leki przeciwbólowe")
            .unit("tabletka")
            .price(89.99)
            .amount(200)
            .status("AVAILABLE")
            .build());

    warehouseStockItemRepository.save(
        WarehouseStockItem.builder()
            .warehouse(warehouse)
            .name("Krople do uszu 10ml")
            .category("Dermatologia")
            .unit("butelka")
            .price(34.50)
            .amount(8)
            .status("LOW_STOCK")
            .build());

    warehouseStockItemRepository.save(
        WarehouseStockItem.builder()
            .warehouse(warehouse)
            .name("Szczepionka przeciwko wściekliźnie")
            .category("Szczepionki")
            .unit("dawka")
            .price(55.00)
            .amount(5)
            .status("LOW_STOCK")
            .build());

    warehouseStockItemRepository.save(
        WarehouseStockItem.builder()
            .warehouse(warehouse)
            .name("Bandaż elastyczny")
            .category("Materiały opatrunkowe")
            .unit("rolka")
            .price(8.99)
            .amount(300)
            .status("AVAILABLE")
            .build());

    warehouseStockItemRepository.save(
        WarehouseStockItem.builder()
            .warehouse(warehouse)
            .name("Rękawice jednorazowe L")
            .category("Środki ochrony")
            .unit("para")
            .price(1.50)
            .amount(500)
            .status("AVAILABLE")
            .build());

    log.info("Warehouse stock items seeded.");
  }
}
