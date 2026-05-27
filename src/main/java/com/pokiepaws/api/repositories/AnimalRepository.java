package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.Owner;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnimalRepository extends JpaRepository<Animal, Long> {

  List<Animal> findAllByOwnerAndActiveTrue(Owner owner);

  Optional<Animal> findByIdAndOwnerAndActiveTrue(Long id, Owner owner);

  Optional<Animal> findByIdAndActiveTrue(Long id);

  List<Animal> findAllByOwnerUserIdAndActiveTrue(Long ownerUserId);

  @Query(
      """
      select distinct a from Animal a
      join Visit v on v.animal = a
      where v.clinic.id = :clinicId
        and a.active = true
      order by a.name asc
      """)
  List<Animal> findDistinctActivePatientsByClinicId(@Param("clinicId") Long clinicId);

  Optional<Animal> findByMicrochipNumber(String microchipNumber);
}
