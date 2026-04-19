package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.repositories.VetRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VetService {

  private final VetRepository vetRepository;

  public List<Vet> getAll() {
    return vetRepository.findAll();
  }

  public Vet getById(Long id) {
    return vetRepository.findById(id).orElseThrow(() -> new RuntimeException("Vet not found"));
  }

  public List<Vet> getByClinic(Long clinicId) {
    return vetRepository.findAllByClinicId(clinicId);
  }

  public Vet save(Vet vet) {
    return vetRepository.save(vet);
  }

  public void delete(Long id) {
    vetRepository.deleteById(id);
  }
}
