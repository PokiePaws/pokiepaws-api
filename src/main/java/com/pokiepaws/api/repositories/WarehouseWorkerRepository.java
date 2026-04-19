package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.WarehouseWorker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseWorkerRepository extends JpaRepository<WarehouseWorker, Long> {
  Optional<WarehouseWorker> findByUser_Email(String email);
}
