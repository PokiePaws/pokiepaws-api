package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.models.Vet;
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
public class AvailabilityService {

  private static final int SLOT_MINUTES = 30;
  private static final LocalTime WORK_START = LocalTime.of(9, 0);
  private static final LocalTime WORK_END = LocalTime.of(17, 0);

  private final VetRepository vetRepository;
  private final VisitRepository visitRepository;

  @Transactional(readOnly = true)
  public AvailableSlotsResponse getAvailableSlots(Long clinicId, Long vetUserId, LocalDate date) {
    Vet vet =
        vetRepository
            .findById(vetUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vet not found"));

    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinicId)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Selected vet does not belong to selected clinic");
    }

    LocalDateTime dayStart = date.atTime(WORK_START);
    LocalDateTime dayEnd = date.atTime(WORK_END);

    var visits = visitRepository.findAllByVetUserIdAndStartsAtBetween(vetUserId, dayStart, dayEnd);

    List<LocalDateTime> available = new ArrayList<>();

    for (LocalDateTime slotStart = dayStart;
        slotStart.plusMinutes(SLOT_MINUTES).isBefore(dayEnd.plusNanos(1));
        slotStart = slotStart.plusMinutes(SLOT_MINUTES)) {

      LocalDateTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);

      LocalDateTime finalSlotStart = slotStart;
      boolean overlaps =
          visits.stream()
              .filter(v -> v.getStatus() != com.pokiepaws.api.models.VisitStatus.CANCELLED)
              .anyMatch(
                  v -> finalSlotStart.isBefore(v.getEndsAt()) && slotEnd.isAfter(v.getStartsAt()));

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
}
