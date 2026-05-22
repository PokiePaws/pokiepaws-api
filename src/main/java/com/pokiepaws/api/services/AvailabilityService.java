package com.pokiepaws.api.services;

import com.pokiepaws.api.config.properties.VisitScheduleProperties;
import com.pokiepaws.api.dto.visit.AvailableSlotsResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
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
public class AvailabilityService {

  private final VetRepository vetRepository;
  private final VisitRepository visitRepository;
  private final VisitScheduleProperties visitScheduleProperties;

  @Transactional(readOnly = true)
  public AvailableSlotsResponse getAvailableSlots(Long clinicId, Long vetUserId, LocalDate date) {

    Vet vet =
            vetRepository
                    .findById(vetUserId)
                    .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VET_NOT_FOUND));

    if (vet.getClinic() == null || !vet.getClinic().getId().equals(clinicId)) {
      throw ApiException.badRequest(ApiErrorMessage.SELECTED_VET_DOES_NOT_BELONG_TO_CLINIC);
    }

    int slotMinutes = visitScheduleProperties.getSlotMinutes();

    LocalDateTime dayStart =
            date.atTime(visitScheduleProperties.getWorkStart());

    LocalDateTime dayEnd =
            date.atTime(visitScheduleProperties.getWorkEnd());

    List<Visit> visits =
            visitRepository.findAllByVetUserIdAndStartsAtBetween(
                    vetUserId,
                    dayStart,
                    dayEnd);

    List<LocalDateTime> available = new ArrayList<>();

    for (LocalDateTime slotStart = dayStart;
         slotStart.plusMinutes(slotMinutes).isBefore(dayEnd.plusNanos(1));
         slotStart = slotStart.plusMinutes(slotMinutes)) {

      LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);

      if (!hasOverlappingVisit(visits, slotStart, slotEnd)) {
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

  private boolean hasOverlappingVisit(
          List<Visit> visits,
          LocalDateTime slotStart,
          LocalDateTime slotEnd) {

    return visits.stream()
            .filter(visit -> visit.getStatus() != VisitStatus.CANCELLED)
            .anyMatch(
                    visit->
                            slotStart.isBefore(visit.getEndsAt())
                                    && slotEnd.isAfter(visit.getStartsAt()));
  }
}