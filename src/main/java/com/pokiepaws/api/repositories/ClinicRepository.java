package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
}
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {
  Optional<Clinic> findByClinicNameIgnoreCase(String clinicName);

  List<Clinic> findAllByCity(String city);
}
