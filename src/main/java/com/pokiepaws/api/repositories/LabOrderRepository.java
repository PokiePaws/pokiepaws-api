package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.LabOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderRepository extends JpaRepository<LabOrder, Long> {
  List<LabOrder> findAllByClinicIdOrderByOrderedAtDesc(Long clinicId);

  List<LabOrder> findAllByVetUserIdOrderByOrderedAtDesc(Long vetUserId);

  List<LabOrder> findAllByAnimalIdOrderByOrderedAtDesc(Long animalId);
}
