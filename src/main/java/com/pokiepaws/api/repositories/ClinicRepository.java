package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Clinic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    Optional<Clinic> findByClinicNameIgnoreCase(String clinicName);
}