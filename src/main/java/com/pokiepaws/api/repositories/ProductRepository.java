package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findByName(String name);
}
