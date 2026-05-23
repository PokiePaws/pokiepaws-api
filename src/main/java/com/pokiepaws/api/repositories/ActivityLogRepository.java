package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.ActivityLog;
import com.pokiepaws.api.models.ActivityLog.LogType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

  List<ActivityLog> findAllByOrderByTimeDesc(Pageable pageable);

  List<ActivityLog> findByTypeOrderByTimeDesc(LogType type, Pageable pageable);

  long countByTimeAfter(LocalDateTime from);

  long countByType(LogType type);
}
