package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Product;
import com.pokiepaws.api.repositories.ProductRepository;
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
public class ProductSeeder implements Seeder {

  private final ProductRepository productRepository;
  private static final String TABLET = "tablet";
  private static final String CAPSULE = "capsule";
  private static final String SUSPENSION = "suspension";
  private static final String PIPETTE = "pipette";
  private static final String VIAL = "vial";
  private static final String TUBE = "tube";
  private static final String GEL = "gel";

  @Override
  public int order() {
    return 20;
  }

  @Override
  @Transactional
  public void seed() {
    List<Product> products =
        List.of(
            Product.builder().name("Amoksycylina 500mg").unit(TABLET).active(true).build(),
            Product.builder().name("Amoksycylina 250mg").unit(TABLET).active(true).build(),
            Product.builder().name("Enrofloksacyna 50mg").unit(TABLET).active(true).build(),
            Product.builder().name("Enrofloksacyna 150mg").unit(TABLET).active(true).build(),
            Product.builder().name("Metronidazol 250mg").unit(TABLET).active(true).build(),
            Product.builder().name("Doksycyklina 100mg").unit(TABLET).active(true).build(),
            Product.builder().name("Cefaleksyna 300mg").unit(TABLET).active(true).build(),
            Product.builder().name("Klindamycyna 150mg").unit(TABLET).active(true).build(),
            Product.builder().name("Karprofen 50mg").unit(TABLET).active(true).build(),
            Product.builder().name("Karprofen 25mg").unit(TABLET).active(true).build(),
            Product.builder().name("Meloksykam 1mg").unit(TABLET).active(true).build(),
            Product.builder().name("Meloksykam 2,5mg/ml").unit(SUSPENSION).active(true).build(),
            Product.builder().name("Mavacoxib 6mg").unit(TABLET).active(true).build(),
            Product.builder().name("Mavacoxib 30mg").unit(TABLET).active(true).build(),
            Product.builder().name("Tramadol 50mg").unit(TABLET).active(true).build(),
            Product.builder().name("Metamizol 500mg").unit(TABLET).active(true).build(),
            Product.builder().name("Fenbendazol 150mg").unit(TABLET).active(true).build(),
            Product.builder()
                .name("Milbemycyna oksym + prazykwantel")
                .unit(TABLET)
                .active(true)
                .build(),
            Product.builder().name("Selamektyna 45mg").unit(PIPETTE).active(true).build(),
            Product.builder().name("Fipronil + S-metopren").unit(PIPETTE).active(true).build(),
            Product.builder().name("Afoksolaner 28,3mg").unit(TABLET).active(true).build(),
            Product.builder().name("Fluralaner 112,5mg").unit(TABLET).active(true).build(),
            Product.builder().name("Pyrantel 144mg").unit(TABLET).active(true).build(),
            Product.builder().name("Prednizolon 5mg").unit(TABLET).active(true).build(),
            Product.builder().name("Deksametazon 0,5mg").unit(TABLET).active(true).build(),
            Product.builder().name("Oclacitinib 3,6mg").unit(TABLET).active(true).build(),
            Product.builder().name("Maść antybiotykowa uszna 15g").unit(TUBE).active(true).build(),
            Product.builder()
                .name("Krople oczne z chloramfenikolem")
                .unit(VIAL)
                .active(true)
                .build(),
            Product.builder().name("Omeprazol 10mg").unit(CAPSULE).active(true).build(),
            Product.builder().name("Famotydyna 10mg").unit(TABLET).active(true).build(),
            Product.builder().name("Metoklopramid 10mg").unit(TABLET).active(true).build(),
            Product.builder().name("Maropitant 16mg").unit(TABLET).active(true).build(),
            Product.builder().name("Loperamid 2mg").unit(TABLET).active(true).build(),
            Product.builder().name("Węgiel aktywny 250mg").unit(TABLET).active(true).build(),
            Product.builder().name("Pimobendan 1,25mg").unit(TABLET).active(true).build(),
            Product.builder().name("Pimobendan 5mg").unit(TABLET).active(true).build(),
            Product.builder().name("Enalapril 2,5mg").unit(TABLET).active(true).build(),
            Product.builder().name("Furosemid 40mg").unit(TABLET).active(true).build(),
            Product.builder().name("Spironolakton 25mg").unit(TABLET).active(true).build(),
            Product.builder().name("Atenolol 25mg").unit(TABLET).active(true).build(),
            Product.builder().name("Lewotyroksyna 0,1mg").unit(TABLET).active(true).build(),
            Product.builder().name("Trilostane 60mg").unit(CAPSULE).active(true).build(),
            Product.builder().name("Metymazol 5mg").unit(TABLET).active(true).build(),
            Product.builder().name("Insulina canis 40 IU/ml").unit(VIAL).active(true).build(),
            Product.builder().name("Fenobarbital 30mg").unit(TABLET).active(true).build(),
            Product.builder().name("Imepitoin 400mg").unit(TABLET).active(true).build(),
            Product.builder().name("Gabapentyna 100mg").unit(CAPSULE).active(true).build(),
            Product.builder().name("Acepromazyna 25mg").unit(TABLET).active(true).build(),
            Product.builder().name("Caniviton Plus").unit(TABLET).active(true).build(),
            Product.builder().name("Synulox 250mg").unit(TABLET).active(true).build(),
            Product.builder().name("Hepatiale Forte").unit(TABLET).active(true).build(),
            Product.builder().name("Zylkene 75mg").unit(CAPSULE).active(true).build(),
            Product.builder().name("Krople do uszu Osurnia").unit(GEL).active(true).build());

    log.info("ProductSeeder started. productsToEnsure={}", products.size());

    int created = 0;

    for (Product p : products) {
      boolean exists = productRepository.findByName(p.getName()).isPresent();
      if (!exists) {
        productRepository.save(p);
        created++;
        log.info("Product created: {}", p.getName());
      }
    }

    log.info("ProductSeeder finished. createdProducts={}", created);
  }
}
