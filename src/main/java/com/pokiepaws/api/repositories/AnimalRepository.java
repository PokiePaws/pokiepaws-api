package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  List<Animal> findAllByActiveTrue();

  List<Animal> findAllByOwnerAndActiveTrue(Owner owner);

  Optional<Animal> findByIdAndOwnerAndActiveTrue(Long id, Owner owner);

  List<Animal> findAllByOwnerUserIdAndActiveTrue(Long ownerUserId);

  Optional<Animal> findByMicrochipNumber(String microchipNumber);

  @Query(
      "SELECT DISTINCT a FROM Animal a JOIN Visit v ON v.animal = a WHERE v.clinic.id = :clinicId AND a.active = true ORDER BY a.name")
  List<Animal> findDistinctByClinicIdViaVisits(@Param("clinicId") Long clinicId);
}
