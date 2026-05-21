package com.pokiepaws.api.config.datainit.seeders;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.ClinicAssortmentItem;
import com.pokiepaws.api.repositories.ClinicAssortmentItemRepository;
import com.pokiepaws.api.repositories.ClinicRepository;
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
public class ClinicAssortmentItemSeeder implements Seeder {

  private final ClinicRepository clinicRepository;
  private final ClinicAssortmentItemRepository clinicAssortmentItemRepository;

  @Override
  public int order() {
    return 60;
  }

  @Override
  @Transactional
  public void seed() {
    if (clinicAssortmentItemRepository.count() > 30) {
      log.info("Clinic assortment items already seeded.");
      return;
    }

    List<Clinic> clinics = clinicRepository.findAll();
    if (clinics.isEmpty()) {
      log.warn("No clinics found, skipping clinic assortment seeder.");
      return;
    }

    Clinic warszawa =
        clinics.stream()
            .filter(c -> c.getClinicName().contains("Warszawa"))
            .findFirst()
            .orElse(clinics.get(0));

    Clinic krakow =
        clinics.stream()
            .filter(c -> c.getClinicName().contains("Kraków"))
            .findFirst()
            .orElse(clinics.get(0));

    List<ClinicAssortmentItem> items =
        List.of(
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Amoksycylina 500mg")
                .category("Antybiotyki")
                .amount(20)
                .unit("opakowanie")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Bandaż elastyczny")
                .category("Materiały opatrunkowe")
                .amount(50)
                .unit("rolka")
                .status("IN_PROGRESS")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Rękawice jednorazowe L")
                .category("Środki ochrony")
                .amount(100)
                .unit("para")
                .status("SHIPPED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Strzykawki 5ml")
                .category("Sprzęt jednorazowy")
                .amount(200)
                .unit("szt")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Igły iniekcyjne 0,8mm")
                .category("Sprzęt jednorazowy")
                .amount(500)
                .unit("szt")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Płyn Ringera 500ml")
                .category("Płyny infuzyjne")
                .amount(30)
                .unit("butelka")
                .status("IN_PROGRESS")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Gaza opatrunkowa")
                .category("Materiały opatrunkowe")
                .amount(80)
                .unit("opakowanie")
                .status("DELIVERED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Metronidazol 250mg")
                .category("Antybiotyki")
                .amount(40)
                .unit("tabletka")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Witamina B12")
                .category("Suplementy")
                .amount(60)
                .unit("ampułka")
                .status("SHIPPED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Plaster opatrunkowy")
                .category("Materiały opatrunkowe")
                .amount(150)
                .unit("szt")
                .status("REJECTED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Chlorheksydyna 0,5%")
                .category("Dezynfekcja")
                .amount(10)
                .unit("butelka")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Kołnierz elżbietański M")
                .category("Akcesoria")
                .amount(5)
                .unit("szt")
                .status("IN_PROGRESS")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Papier do EKG")
                .category("Materiały diagnostyczne")
                .amount(20)
                .unit("rolka")
                .status("DELIVERED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Laktuloza 667mg")
                .category("Gastroenterologia")
                .amount(15)
                .unit("opakowanie")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(warszawa)
                .name("Furosemid 40mg")
                .category("Kardiologia")
                .amount(100)
                .unit("tabletka")
                .status("SHIPPED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Karprogen 50mg")
                .category("Leki przeciwbólowe")
                .amount(30)
                .unit("tabletka")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Krople do uszu 10ml")
                .category("Dermatologia")
                .amount(10)
                .unit("butelka")
                .status("DELIVERED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Szczepionka wścieklizna")
                .category("Szczepionki")
                .amount(12)
                .unit("dawka")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Deksametazon 2mg")
                .category("Steroidy")
                .amount(50)
                .unit("tabletka")
                .status("IN_PROGRESS")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Maska tlenowa dla zwierząt")
                .category("Sprzęt medyczny")
                .amount(3)
                .unit("szt")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Środek do dezynfekcji rąk")
                .category("Dezynfekcja")
                .amount(20)
                .unit("butelka")
                .status("SHIPPED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Probiotyki dla psów")
                .category("Gastroenterologia")
                .amount(25)
                .unit("opakowanie")
                .status("PENDING")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Worki na odpady medyczne")
                .category("Gospodarka odpadami")
                .amount(100)
                .unit("szt")
                .status("DELIVERED")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Cefovecin 80mg")
                .category("Antybiotyki")
                .amount(8)
                .unit("fiolka")
                .status("IN_PROGRESS")
                .build(),
            ClinicAssortmentItem.builder()
                .clinic(krakow)
                .name("Foliowy preparat dermatologiczny")
                .category("Dermatologia")
                .amount(15)
                .unit("tuba")
                .status("REJECTED")
                .build());

    clinicAssortmentItemRepository.saveAll(items);
    log.info("Clinic assortment items seeded.");
  }
}
