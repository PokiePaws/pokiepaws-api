package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VisitRepository extends JpaRepository<Visit, Long> {

  List<Visit> findAllByAnimalId(Long animalId);

  List<Visit> findAllByVetUserId(Long vetUserId);

  List<Visit> findAllByClinicId(Long clinicId);

  List<Visit> findAllByStatus(VisitStatus status);

  List<Visit> findAllByVetUserIdAndStatusNotAndStartsAtAfterOrderByStartsAtAsc(
      Long vetUserId, VisitStatus status, LocalDateTime from);

  List<Visit> findAllByVetUserIdAndStatusNotAndStartsAtBetweenOrderByStartsAtAsc(
      Long vetUserId, VisitStatus status, LocalDateTime from, LocalDateTime to);

  List<Visit> findAllByVetUserIdAndStartsAtBetween(
      Long vetUserId, LocalDateTime from, LocalDateTime to);

  @Query(
      """
      select visit from Visit visit
      where visit.vet.userId = :vetUserId
        and visit.status <> com.pokiepaws.api.models.VisitStatus.CANCELLED
        and :start < visit.endsAt
        and :end > visit.startsAt
      """)
  List<Visit> findOverlappingVisits(Long vetUserId, LocalDateTime start, LocalDateTime end);

  List<Visit> findAllByAnimalOwnerUserIdAndStartsAtAfterOrderByStartsAtAsc(
      Long ownerUserId, LocalDateTime from);

  List<Visit> findAllByAnimalOwnerUserIdAndStartsAtBetween(
      Long ownerUserId, LocalDateTime from, LocalDateTime to);
}
