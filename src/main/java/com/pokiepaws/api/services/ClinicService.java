package com.pokiepaws.api.services;

import com.pokiepaws.api.models.ActivityLog.LogType;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.repositories.ClinicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClinicService {

  private static final String CLINIC_NOT_FOUND = "Clinic not found";

  private final ClinicRepository clinicRepository;
  private final ActivityLogService activityLogService;

  @Transactional(readOnly = true)
  public List<Clinic> getAll() {
    return clinicRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Clinic getById(Long id) {
    return clinicRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, CLINIC_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public List<Clinic> getByCity(String city) {
    return clinicRepository.findAllByCityIgnoreCase(city);
  }

  @Transactional
  public Clinic save(Clinic clinic) {
    boolean isNew = clinic.getId() == null;
    Clinic saved = clinicRepository.save(clinic);

    activityLogService.log(
        LogType.data,
        (isNew ? "Utworzono klinikę: " : "Zaktualizowano klinikę: ") + saved.getClinicName(),
        saved.getClinicName());

    return saved;
  }

  @Transactional
  public void delete(Long id) {
    Clinic clinic = getById(id);
    String name = clinic.getClinicName();

    clinicRepository.delete(clinic);
    activityLogService.log(LogType.data, "Usunięto klinikę: " + name, name);
  }
}
