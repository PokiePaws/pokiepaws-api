package com.pokiepaws.api.services;

import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.repositories.ClinicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicQueryService {

  private final ClinicRepository clinicRepository;

  @Transactional(readOnly = true)
  public List<Clinic> getAllAsDto() {
    return clinicRepository.findAll().stream().filter(Clinic::isActive).toList();
  }

  @Transactional(readOnly = true)
  public List<Clinic> getByCityAsDto(String city) {
    return clinicRepository.findAllByCity(city).stream().filter(Clinic::isActive).toList();
  }

  @Transactional(readOnly = true)
  public Clinic getByIdAsDto(Long id) {
    return clinicRepository
        .findById(id)
        .filter(Clinic::isActive)
        .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.CLINIC_NOT_FOUND));
  }
}
