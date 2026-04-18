package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    List<Visit> findAllByAnimalId(Long animalId);
    List<Visit> findAllByVetUserId(Long vetUserId);
    List<Visit> findAllByClinicId(Long clinicId);
    List<Visit> findAllByClinicIdAndVisitDateBetween(Long clinicId, LocalDate from, LocalDate to);
    List<Visit> findAllByStatus(VisitStatus status);
}