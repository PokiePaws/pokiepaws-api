package com.pokiepaws.api.controllers;

import com.pokiepaws.api.config.OpenApiConfig;
import com.pokiepaws.api.repositories.ProductRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProductController {

  private final ProductRepository productRepository;

  @GetMapping
  @PreAuthorize("hasAnyRole('VET', 'ADMIN')")
  public ResponseEntity<List<ProductResponse>> getProducts() {
    List<ProductResponse> products =
        productRepository.findAllByActiveTrueOrderByName().stream()
            .map(p -> new ProductResponse(p.getId(), p.getName(), p.getUnit()))
            .toList();
    return ResponseEntity.ok(products);
  }

  public record ProductResponse(Long id, String name, String unit) {}
}
