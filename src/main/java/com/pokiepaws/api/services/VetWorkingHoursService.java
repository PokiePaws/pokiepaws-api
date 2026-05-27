package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.vet.VetWorkingHoursRequest;
import com.pokiepaws.api.dto.vet.VetWorkingHoursResponse;
import com.pokiepaws.api.exceptions.ApiErrorMessage;
import com.pokiepaws.api.exceptions.ApiException;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.models.VetWorkingHours;
import com.pokiepaws.api.repositories.VetRepository;
import com.pokiepaws.api.repositories.VetWorkingHoursRepository;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VetWorkingHoursService {

  private final VetRepository vetRepository;
  private final VetWorkingHoursRepository workingHoursRepository;

  @Transactional(readOnly = true)
  public List<VetWorkingHoursResponse> getCurrentVetWorkingHours(String email) {
    Vet vet = getVetByEmail(email);
    return workingHoursRepository.findAllByVetUserIdOrderByDayOfWeekAsc(vet.getUserId()).stream()
        .sorted(Comparator.comparing(VetWorkingHours::getDayOfWeek))
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public List<VetWorkingHoursResponse> replaceCurrentVetWorkingHours(
      String email, List<VetWorkingHoursRequest> requests) {
    Vet vet = getVetByEmail(email);
    requests.forEach(this::validate);

    workingHoursRepository.deleteAllByVetUserId(vet.getUserId());
    workingHoursRepository.flush();

    List<VetWorkingHours> saved =
        requests.stream()
            .map(
                request ->
                    VetWorkingHours.builder()
                        .vet(vet)
                        .dayOfWeek(request.getDayOfWeek())
                        .startTime(request.getStartTime())
                        .endTime(request.getEndTime())
                        .breakStart(request.getBreakStart())
                        .breakEnd(request.getBreakEnd())
                        .active(request.isActive())
                        .build())
            .map(workingHoursRepository::save)
            .toList();

    return saved.stream().map(this::toResponse).toList();
  }

  private Vet getVetByEmail(String email) {
    return vetRepository
        .findByUserEmail(email)
        .orElseThrow(() -> ApiException.notFound(ApiErrorMessage.VET_NOT_FOUND));
  }

  private void validate(VetWorkingHoursRequest request) {
    if (!request.getStartTime().isBefore(request.getEndTime())) {
      throw ApiException.badRequest("Work start must be before work end");
    }

    LocalTime breakStart = request.getBreakStart();
    LocalTime breakEnd = request.getBreakEnd();
    if (breakStart == null && breakEnd == null) {
      return;
    }

    if (breakStart == null || breakEnd == null || !breakStart.isBefore(breakEnd)) {
      throw ApiException.badRequest("Break start must be before break end");
    }

    if (breakStart.isBefore(request.getStartTime()) || breakEnd.isAfter(request.getEndTime())) {
      throw ApiException.badRequest("Break must be within work hours");
    }
  }

  private VetWorkingHoursResponse toResponse(VetWorkingHours workingHours) {
    return VetWorkingHoursResponse.builder()
        .id(workingHours.getId())
        .vetUserId(workingHours.getVet().getUserId())
        .dayOfWeek(workingHours.getDayOfWeek())
        .startTime(workingHours.getStartTime())
        .endTime(workingHours.getEndTime())
        .breakStart(workingHours.getBreakStart())
        .breakEnd(workingHours.getBreakEnd())
        .active(workingHours.isActive())
        .build();
  }
}
