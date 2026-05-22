package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.models.WarehouseStockItem;
import com.pokiepaws.api.repositories.WarehouseRepository;
import com.pokiepaws.api.repositories.WarehouseStockItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local", "prod"})
public class WarehouseStockItemSeeder implements Seeder {

  private static final String CATEGORY_ANTIBIOTICS = "Antybiotyki";
  private static final String CATEGORY_DRESSING_MATERIALS =
          "Materiały opatrunkowe";

  private static final String UNIT_PACKAGE = "opakowanie";
  private static final String UNIT_TABLET = "tabletka";
  private static final String UNIT_BOTTLE = "butelka";

  private static final String STATUS_AVAILABLE = "AVAILABLE";
  private static final String STATUS_LOW_STOCK = "LOW_STOCK";

  private final WarehouseRepository warehouseRepository;
  private final WarehouseStockItemRepository warehouseStockItemRepository;

  @Override
  public int order() {
    return 55;
  }

  @Override
  @Transactional
  public void seed() {
    Warehouse warehouse =
            warehouseRepository
                    .findByWarehouseNameIgnoreCase(WarehousesSeeder.WAREHOUSE_MAIN)
                    .orElse(null);

    if (warehouse == null) {
      log.warn("Warehouse not found, skipping warehouse stock seeder.");
      return;
    }

    List<WarehouseStockItem> items =
            List.of(
                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Amoksycylina 500mg")
                            .category(CATEGORY_ANTIBIOTICS)
                            .unit(UNIT_PACKAGE)
                            .price(24.99)
                            .amount(150)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Karprogen 50mg")
                            .category("Leki przeciwbólowe")
                            .unit(UNIT_TABLET)
                            .price(89.99)
                            .amount(200)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Krople do uszu 10ml")
                            .category("Dermatologia")
                            .unit(UNIT_BOTTLE)
                            .price(34.50)
                            .amount(8)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Szczepionka wścieklizna")
                            .category("Szczepionki")
                            .unit("dawka")
                            .price(55.00)
                            .amount(5)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Bandaż elastyczny")
                            .category(CATEGORY_DRESSING_MATERIALS)
                            .unit("rolka")
                            .price(8.99)
                            .amount(300)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Rękawice jednorazowe L")
                            .category("Środki ochrony")
                            .unit("para")
                            .price(1.50)
                            .amount(500)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Strzykawki 5ml")
                            .category("Sprzęt jednorazowy")
                            .unit("szt")
                            .price(0.80)
                            .amount(1000)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Igły iniekcyjne 0,8mm")
                            .category("Sprzęt jednorazowy")
                            .unit("szt")
                            .price(0.30)
                            .amount(2000)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Płyn Ringera 500ml")
                            .category("Płyny infuzyjne")
                            .unit(UNIT_BOTTLE)
                            .price(12.00)
                            .amount(80)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Gaza opatrunkowa")
                            .category(CATEGORY_DRESSING_MATERIALS)
                            .unit(UNIT_PACKAGE)
                            .price(5.50)
                            .amount(400)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Metronidazol 250mg")
                            .category(CATEGORY_ANTIBIOTICS)
                            .unit(UNIT_TABLET)
                            .price(0.60)
                            .amount(600)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Witamina B12")
                            .category("Suplementy")
                            .unit("ampułka")
                            .price(3.20)
                            .amount(7)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Plaster opatrunkowy")
                            .category(CATEGORY_DRESSING_MATERIALS)
                            .unit("szt")
                            .price(0.40)
                            .amount(900)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Chlorheksydyna 0,5%")
                            .category("Dezynfekcja")
                            .unit(UNIT_BOTTLE)
                            .price(18.00)
                            .amount(60)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Kołnierz elżbietański M")
                            .category("Akcesoria")
                            .unit("szt")
                            .price(22.00)
                            .amount(4)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Papier do EKG")
                            .category("Materiały diagnostyczne")
                            .unit("rolka")
                            .price(15.00)
                            .amount(50)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Laktuloza 667mg")
                            .category("Gastroenterologia")
                            .unit(UNIT_PACKAGE)
                            .price(28.00)
                            .amount(120)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Furosemid 40mg")
                            .category("Kardiologia")
                            .unit(UNIT_TABLET)
                            .price(0.90)
                            .amount(3)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Deksametazon 2mg")
                            .category("Steroidy")
                            .unit(UNIT_TABLET)
                            .price(1.20)
                            .amount(250)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Środek do dezynfekcji rąk")
                            .category("Dezynfekcja")
                            .unit(UNIT_BOTTLE)
                            .price(9.50)
                            .amount(180)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Probiotyki dla psów")
                            .category("Gastroenterologia")
                            .unit(UNIT_PACKAGE)
                            .price(45.00)
                            .amount(70)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Worki na odpady medyczne")
                            .category("Gospodarka odpadami")
                            .unit("szt")
                            .price(2.00)
                            .amount(6)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Cefovecin 80mg")
                            .category(CATEGORY_ANTIBIOTICS)
                            .unit("fiolka")
                            .price(120.00)
                            .amount(30)
                            .status(STATUS_AVAILABLE)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Preparat dermatologiczny")
                            .category("Dermatologia")
                            .unit("tuba")
                            .price(38.00)
                            .amount(9)
                            .status(STATUS_LOW_STOCK)
                            .build(),

                    WarehouseStockItem.builder()
                            .warehouse(warehouse)
                            .name("Linia infuzyjna")
                            .category("Sprzęt medyczny")
                            .unit("szt")
                            .price(6.50)
                            .amount(150)
                            .status(STATUS_AVAILABLE)
                            .build());

    int added = 0;

    for (WarehouseStockItem item : items) {
      if (!warehouseStockItemRepository.existsByNameIgnoreCaseAndWarehouse_Id(
              item.getName(), warehouse.getId())) {

        warehouseStockItemRepository.save(item);
        added++;
      }
    }

    if (added > 0) {
      log.info("Warehouse stock items seeded: {} new items added.", added);
    } else {
      log.info("Warehouse stock items already up to date.");
    }
  }
}