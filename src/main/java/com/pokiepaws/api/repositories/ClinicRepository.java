package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
    List<Clinic> findAllByCity(String city);
}