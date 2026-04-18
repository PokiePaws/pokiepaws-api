package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Vet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VetRepository extends JpaRepository<Vet, Long> {
}