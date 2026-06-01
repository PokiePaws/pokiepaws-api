package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.ClinicStockItem;
import com.pokiepaws.api.models.WarehouseStockItem;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.ClinicStockItemRepository;
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
public class ClinicStockSeeder implements Seeder {

  private final ClinicRepository clinicRepository;
  private final WarehouseStockItemRepository warehouseStockItemRepository;
  private final ClinicStockItemRepository clinicStockItemRepository;

  @Override
  public int order() {
    return 30;
  }

  @Override
  @Transactional
  public void seed() {
    int defaultQty = 50;

    List<WarehouseStockItem> stockItems = warehouseStockItemRepository.findAll();
    List<Clinic> clinics = clinicRepository.findAll();

    log.info(
        "ClinicStockSeeder started. clinics={}, stockItems={}, defaultQty={}",
        clinics.size(),
        stockItems.size(),
        defaultQty);

    int created = 0;

    for (Clinic clinic : clinics) {
      for (WarehouseStockItem stockItem : stockItems) {
        boolean exists =
            clinicStockItemRepository
                .findByClinicIdAndStockItemId(clinic.getId(), stockItem.getId())
                .isPresent();

        if (!exists) {
          clinicStockItemRepository.save(
              ClinicStockItem.builder()
                  .clinic(clinic)
                  .stockItem(stockItem)
                  .quantityPackages(defaultQty)
                  .build());
          created++;
        }
      }
    }

    log.info("ClinicStockSeeder finished. createdStockItems={}", created);
  }
}
