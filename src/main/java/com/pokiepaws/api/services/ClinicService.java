package com.pokiepaws.api.services;

import com.pokiepaws.api.config.properties.VisitScheduleProperties;
import com.pokiepaws.api.dto.vet.VetResponse;
import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicService {

  private final ClinicRepository clinicRepository;
  private final VetRepository vetRepository;
  private final VisitRepository visitRepository;
  private final VisitScheduleProperties visitScheduleProperties;

  public List<Clinic> getAll() {
    return clinicRepository.findAll();
  }

  @Transactional(readOnly = true)
  public List<Clinic> getAllAsDto() {
    return clinicRepository.findAll().stream().filter(Clinic::isActive).toList();
  }

  public Clinic getById(Long id) {
    return clinicRepository
        .findById(id)
        .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.CLINIC_NOT_FOUND));
  }

  public List<Clinic> getByCity(String city) {
    return clinicRepository.findAllByCity(city);
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

  @Transactional(readOnly = true)
  public List<VetResponse> getVetsByClinicId(Long clinicId) {
    getByIdAsDto(clinicId);
    return vetRepository.findAllByClinicId(clinicId).stream()
        .map(
            vet ->
                VetResponse.builder()
                    .id(vet.getUserId())
                    .email(vet.getUser() != null ? vet.getUser().getEmail() : null)
                    .firstName(vet.getFirstName())
                    .lastName(vet.getLastName())
                    .phone(vet.getPhone())
                    .npwz(vet.getNpwz())
                    .specialization(vet.getSpecialization())
                    .clinicName(vet.getClinic() != null ? vet.getClinic().getClinicName() : null)
                    .build())
        .toList();
  }

  @Transactional(readOnly = true)
  public AvailableSlotsResponse getAvailableSlots(Long clinicId, Long vetUserId, LocalDate date) {
    Clinic clinic = getByIdAsDto(clinicId);
    var vet =
        vetRepository
            .findById(vetUserId)
            .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VET_NOT_FOUND));

    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinic.getId())) {
      throw ApiException.badRequest(ApiErrorMessage.SELECTED_VET_DOES_NOT_BELONG_TO_CLINIC);
    }

    int slotMinutes = visitScheduleProperties.getSlotMinutes();
    LocalDateTime dayStart = date.atTime(visitScheduleProperties.getWorkStart());
    LocalDateTime dayEnd = date.atTime(visitScheduleProperties.getWorkEnd());
    var visits = visitRepository.findAllByVetUserIdAndStartsAtBetween(vetUserId, dayStart, dayEnd);

    List<LocalDateTime> available = new ArrayList<>();
    for (LocalDateTime slotStart = dayStart;
        !slotStart.plusMinutes(slotMinutes).isAfter(dayEnd);
        slotStart = slotStart.plusMinutes(slotMinutes)) {
      LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);
      LocalDateTime finalSlotStart = slotStart;
      boolean overlaps =
          visits.stream()
              .filter(visit -> visit.getStatus() != VisitStatus.CANCELLED)
              .anyMatch(
                  visit ->
                      finalSlotStart.isBefore(visit.getEndsAt())
                          && slotEnd.isAfter(visit.getStartsAt()));
      if (!overlaps) {
        available.add(slotStart);
      }
    }

    return AvailableSlotsResponse.builder()
        .clinicId(clinicId)
        .vetUserId(vetUserId)
        .date(date)
        .slotMinutes(slotMinutes)
        .workdayStart(dayStart)
        .workdayEnd(dayEnd)
        .availableStarts(available)
        .build();
  }

  public Clinic save(Clinic clinic) {
    return clinicRepository.save(clinic);
  }

  public void delete(Long id) {
    clinicRepository.deleteById(id);
  }
}
