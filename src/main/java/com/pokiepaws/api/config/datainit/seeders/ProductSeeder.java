package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Product;
import com.pokiepaws.api.repositories.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductSeeder implements Seeder {

  private final ProductRepository productRepository;

  @Override
  public int order() {
    return 20;
  }

  @Override
  @Transactional
  public void seed() {
    List<Product> products =
        List.of(
            Product.builder().name("Amoxicillin 500mg").unit("capsule").active(true).build(),
            Product.builder().name("Carprofen 50mg").unit("tablet").active(true).build(),
            Product.builder().name("Ear drops 10ml").unit("bottle").active(true).build(),
            Product.builder().name("Caniviton").unit("tablet").active(true).build());

    for (Product p : products) {
      productRepository.findByName(p.getName()).orElseGet(() -> productRepository.save(p));
    }
  }
}
