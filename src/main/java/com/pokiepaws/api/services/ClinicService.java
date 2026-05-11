package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.vet.VetResponse;
import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.VisitStatus;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClinicService {

  private static final int SLOT_MINUTES = 30;
  private static final LocalTime WORK_START = LocalTime.of(9, 0);
  private static final LocalTime WORK_END = LocalTime.of(17, 0);

  private final ClinicRepository clinicRepository;
  private final VetRepository vetRepository;
  private final VisitRepository visitRepository;

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
        .orElseThrow(() -> new RuntimeException("Clinic not found"));
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
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));
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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vet not found"));

    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinic.getId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Vet does not belong to this clinic");
    }

    LocalDateTime dayStart = date.atTime(WORK_START);
    LocalDateTime dayEnd = date.atTime(WORK_END);
    var visits = visitRepository.findAllByVetUserIdAndStartsAtBetween(vetUserId, dayStart, dayEnd);

    List<LocalDateTime> available = new ArrayList<>();
    for (LocalDateTime slotStart = dayStart;
        !slotStart.plusMinutes(SLOT_MINUTES).isAfter(dayEnd);
        slotStart = slotStart.plusMinutes(SLOT_MINUTES)) {
      LocalDateTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);
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
        .slotMinutes(SLOT_MINUTES)
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
