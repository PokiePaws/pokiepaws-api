package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Animal;
import com.pokiepaws.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {
    List<Animal> findAllByOwnerAndActiveTrue(User owner);
    Optional<Animal> findByIdAndOwner(Long id, User owner);
    Optional<Animal> findByMicrochipNumber(String microchipNumber);
    List<Animal> findAllByOwnerIdAndActiveTrue(Long ownerId);
}