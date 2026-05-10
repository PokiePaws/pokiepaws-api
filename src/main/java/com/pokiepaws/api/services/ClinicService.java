package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.clinic.ClinicResponse;
import com.pokiepaws.api.dto.vet.VetResponse;
import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.models.*;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VisitRepository;
import java.time.*;
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

  // ─────────────────────────────────────────────
  // Istniejące metody — zachowane bez zmian
  // ─────────────────────────────────────────────

  public Clinic save(Clinic clinic) {
    return clinicRepository.save(clinic);
  }

  public void delete(Long id) {
    clinicRepository.deleteById(id);
  }

  // ─────────────────────────────────────────────
  // Nowe metody zwracające DTO
  // ─────────────────────────────────────────────

  @Transactional(readOnly = true)
  public List<ClinicResponse> getAllAsDto() {
    return clinicRepository.findAll().stream()
        .filter(Clinic::isActive)
        .map(ClinicService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public ClinicResponse getByIdAsDto(Long id) {
    return toResponse(
        clinicRepository
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found")));
  }

  @Transactional(readOnly = true)
  public List<ClinicResponse> getByCityAsDto(String city) {
    return clinicRepository.findAllByCity(city).stream()
        .filter(Clinic::isActive)
        .map(ClinicService::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<VetResponse> getVetsByClinicId(Long clinicId) {
    clinicRepository
        .findById(clinicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

    return vetRepository.findAllByClinicId(clinicId).stream()
        .map(ClinicService::toVetResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AvailableSlotsResponse getAvailableSlots(Long clinicId, Long vetUserId, LocalDate date) {

    clinicRepository
        .findById(clinicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

    Vet vet =
        vetRepository
            .findById(vetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vet not found"));

    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinicId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Vet does not belong to this clinic");
    }

    LocalDateTime dayStart = date.atTime(WORK_START);
    LocalDateTime dayEnd = date.atTime(WORK_END);

    // pobierz zajęte sloty (bez CANCELLED)
    List<LocalDateTime> taken =
        visitRepository.findAllByVetUserIdAndStartsAtBetween(vetUserId, dayStart, dayEnd).stream()
            .filter(v -> v.getStatus() != VisitStatus.CANCELLED)
            .map(Visit::getStartsAt)
            .toList();

    // wygeneruj dostępne sloty
    List<LocalDateTime> available = new ArrayList<>();
    LocalDateTime slot = dayStart;
    LocalDateTime now = LocalDateTime.now();

    while (!slot.plusMinutes(SLOT_MINUTES).isAfter(dayEnd)) {
      if (!taken.contains(slot) && slot.isAfter(now)) {
        available.add(slot);
      }
      slot = slot.plusMinutes(SLOT_MINUTES);
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

  // ─────────────────────────────────────────────
  // Mappers
  // ─────────────────────────────────────────────

  static ClinicResponse toResponse(Clinic c) {
    return ClinicResponse.builder()
        .id(c.getId())
        .clinicName(c.getClinicName())
        .street(c.getStreet())
        .houseNumber(c.getHouseNumber())
        .apartmentNumber(c.getApartmentNumber())
        .postalCode(c.getPostalCode())
        .city(c.getCity())
        .country(c.getCountry())
        .phone(c.getPhone())
        .email(c.getEmail())
        .workingHours(c.getWorkingHours())
        .build();
  }

  static VetResponse toVetResponse(Vet v) {
    return VetResponse.builder()
        .id(v.getUserId())
        .email(v.getUser().getEmail())
        .firstName(v.getFirstName())
        .lastName(v.getLastName())
        .phone(v.getPhone())
        .npwz(v.getNpwz())
        .specialization(v.getSpecialization())
        .clinicName(v.getClinic() != null ? v.getClinic().getClinicName() : null)
        .build();
  }
}
