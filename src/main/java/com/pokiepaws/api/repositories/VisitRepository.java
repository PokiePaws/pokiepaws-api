package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.Visit;
import com.pokiepaws.api.models.VisitStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitRepository extends JpaRepository<Visit, Long> {
  List<Visit> findAllByAnimalId(Long animalId);

  List<Visit> findAllByVetUserId(Long vetUserId);

  List<Visit> findAllByVetUserIdAndStartsAtBetween(
      Long vetUserId, LocalDateTime start, LocalDateTime end);

  @Query(
      """
      select v from Visit v
      where v.vet.userId = :vetUserId
        and v.status <> com.pokiepaws.api.models.VisitStatus.CANCELLED
        and v.startsAt < :end
        and v.endsAt > :start
      """)
  List<Visit> findOverlappingVisits(
      @Param("vetUserId") Long vetUserId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  List<Visit> findAllByAnimalOwnerUserIdAndStartsAtAfterOrderByStartsAtAsc(
      Long ownerUserId, LocalDateTime startsAt);

  List<Visit> findAllByAnimalOwnerUserIdAndStartsAtBetween(
      Long ownerUserId, LocalDateTime start, LocalDateTime end);

  List<Visit> findAllByVetUserIdAndStatusNotAndStartsAtAfterOrderByStartsAtAsc(
      Long vetUserId, VisitStatus status, LocalDateTime startsAt);

  List<Visit> findAllByVetUserIdAndStatusNotAndStartsAtBetweenOrderByStartsAtAsc(
      Long vetUserId, VisitStatus status, LocalDateTime start, LocalDateTime end);

  List<Visit> findAllByClinicId(Long clinicId);

  List<Visit> findAllByClinicIdAndStartsAtBetween(Long clinicId, LocalDateTime from, LocalDateTime to);

  List<Visit> findAllByStatus(VisitStatus status);
}
