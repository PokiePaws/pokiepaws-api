package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Vet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VetRepository extends JpaRepository<Vet, Long> {
    List<Vet> findAllByClinicId(Long clinicId);
    List<Vet> findAllBySpecialization(String specialization);
}