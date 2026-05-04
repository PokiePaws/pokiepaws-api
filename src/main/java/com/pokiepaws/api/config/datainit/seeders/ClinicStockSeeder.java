package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.ClinicStockItem;
import com.pokiepaws.api.models.Product;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.ClinicStockItemRepository;
import com.pokiepaws.api.repositories.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ClinicStockSeeder implements Seeder {

  private final ClinicRepository clinicRepository;
  private final ProductRepository productRepository;
  private final ClinicStockItemRepository clinicStockItemRepository;

  @Override
  public int order() {
    return 30;
  }

  @Override
  @Transactional
  public void seed() {
    int defaultQty = 50;

    List<Product> products =
        productRepository.findAll().stream().filter(Product::isActive).toList();
    List<Clinic> clinics = clinicRepository.findAll();

    for (Clinic clinic : clinics) {
      for (Product product : products) {
        clinicStockItemRepository
            .findByClinicIdAndProductId(clinic.getId(), product.getId())
            .orElseGet(
                () ->
                    clinicStockItemRepository.save(
                        ClinicStockItem.builder()
                            .clinic(clinic)
                            .product(product)
                            .quantityPackages(defaultQty)
                            .build()));
      }
    }
  }
}
