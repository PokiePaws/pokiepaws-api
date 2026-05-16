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
@Profile({"dev", "local"})
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
        if (clinicAssortmentItemRepository.count() > 0) {
            log.info("Clinic assortment items already seeded.");
            return;
        }

        List<Clinic> clinics = clinicRepository.findAll();
        if (clinics.isEmpty()) {
            log.warn("No clinics found, skipping clinic assortment seeder.");
            return;
        }

        Clinic warszawa = clinics.stream()
                .filter(c -> c.getClinicName().contains("Warszawa"))
                .findFirst().orElse(clinics.get(0));

        Clinic krakow = clinics.stream()
                .filter(c -> c.getClinicName().contains("Kraków"))
                .findFirst().orElse(clinics.get(0));

        clinicAssortmentItemRepository.save(ClinicAssortmentItem.builder()
                .clinic(warszawa).name("Amoksycylina 500mg").category("Antybiotyki")
                .amount(20).unit("opakowanie").status("PENDING").build());

        clinicAssortmentItemRepository.save(ClinicAssortmentItem.builder()
                .clinic(warszawa).name("Bandaż elastyczny").category("Materiały opatrunkowe")
                .amount(50).unit("rolka").status("IN_PROGRESS").build());

        clinicAssortmentItemRepository.save(ClinicAssortmentItem.builder()
                .clinic(warszawa).name("Rękawice jednorazowe L").category("Środki ochrony")
                .amount(100).unit("para").status("SHIPPED").build());

        clinicAssortmentItemRepository.save(ClinicAssortmentItem.builder()
                .clinic(krakow).name("Karprogen 50mg").category("Leki przeciwbólowe")
                .amount(30).unit("tabletka").status("PENDING").build());

        clinicAssortmentItemRepository.save(ClinicAssortmentItem.builder()
                .clinic(krakow).name("Krople do uszu 10ml").category("Dermatologia")
                .amount(10).unit("butelka").status("DELIVERED").build());

        log.info("Clinic assortment items seeded.");
    }
}