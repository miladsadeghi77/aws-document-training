package com.miladsadeghi.awsdocument.infrastracture.persistance.repository;

import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.OutboxEvent;
import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.OutboxStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatus(OutboxStatus status);
    // Retry only FAILED events that haven't exceeded max retries
    List<OutboxEvent> findByStatusAndRetryCountLessThan(OutboxStatus status, int retryCount);
}
