package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  List<Animal> findAllByOwnerAndActiveTrue(Owner owner);

  Optional<Animal> findByIdAndOwnerAndActiveTrue(Long id, Owner owner);

  List<Animal> findAllByOwnerUserIdAndActiveTrue(Long ownerUserId);

  Optional<Animal> findByMicrochipNumber(String microchipNumber);
}
