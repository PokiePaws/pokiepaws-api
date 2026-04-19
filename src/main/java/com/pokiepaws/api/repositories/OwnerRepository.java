package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Owner;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
  Optional<Owner> findByUserEmail(String email);
}
