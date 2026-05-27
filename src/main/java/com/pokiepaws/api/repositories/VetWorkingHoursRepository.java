package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.VetWorkingHours;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VetWorkingHoursRepository extends JpaRepository<VetWorkingHours, Long> {

  List<VetWorkingHours> findAllByVetUserIdOrderByDayOfWeekAsc(Long vetUserId);

  Optional<VetWorkingHours> findByVetUserIdAndDayOfWeek(Long vetUserId, DayOfWeek dayOfWeek);

  void deleteAllByVetUserId(Long vetUserId);
}
