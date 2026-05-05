package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.ClinicStockItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicStockItemRepository extends JpaRepository<ClinicStockItem, Long> {
  Optional<ClinicStockItem> findByClinicIdAndProductId(Long clinicId, Long productId);
}
