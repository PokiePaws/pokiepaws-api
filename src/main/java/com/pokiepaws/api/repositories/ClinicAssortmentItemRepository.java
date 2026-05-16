package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.ClinicAssortmentItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicAssortmentItemRepository extends JpaRepository<ClinicAssortmentItem, Long> {
  List<ClinicAssortmentItem> findAllByClinicId(Long clinicId);

  List<ClinicAssortmentItem> findAllByStatus(String status);

  List<ClinicAssortmentItem> findAllByClinicIdAndStatus(Long clinicId, String status);
}
