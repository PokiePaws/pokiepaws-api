package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Vet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VetRepository extends JpaRepository<Vet, Long> {

  Optional<Vet> findByUserEmail(String email);

  Optional<Vet> findByNpwz(String npwz);

  boolean existsByNpwz(String npwz);

  List<Vet> findAllByClinicId(Long clinicId);

  List<Vet> findAllBySpecialization(String specialization);
}
