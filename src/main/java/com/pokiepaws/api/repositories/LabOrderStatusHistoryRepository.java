package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.LabOrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderStatusHistoryRepository
    extends JpaRepository<LabOrderStatusHistory, Long> {

  List<LabOrderStatusHistory> findAllByLabOrderIdOrderByChangedAtAsc(Long labOrderId);
}
