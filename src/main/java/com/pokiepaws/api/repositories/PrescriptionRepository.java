package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Prescription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
  Optional<Prescription> findByVisitId(Long visitId);

  boolean existsByVisitId(Long visitId);
}
