package com.miladsadeghi.awsdocument.infrastracture.outbox;

import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.OutboxEvent;
import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.OutboxStatus;
import com.miladsadeghi.awsdocument.infrastracture.persistance.repository.OutboxEventRepository;
import com.miladsadeghi.awsdocument.infrastracture.sns.publisher.SnsMessagePublisher;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.model.SnsException;

@Service
@Slf4j
public class OutboxPublisher {

  private final OutboxEventRepository outboxEventRepository;
  private final SnsMessagePublisher snsMessagePublisher;

  public OutboxPublisher(OutboxEventRepository outboxEventRepository,
      SnsMessagePublisher snsMessagePublisher) {
    this.outboxEventRepository = outboxEventRepository;
    this.snsMessagePublisher = snsMessagePublisher;
  }

  @Scheduled(fixedDelay = 5000)
  @Transactional
  public void publishPendingEvents() {
    List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxStatus.PENDING);

    if (pendingEvents.isEmpty()) {
      return;
    }
    log.info("Found {} pending outbox events", pendingEvents.size());

    for (OutboxEvent event : pendingEvents) {
      try {
        snsMessagePublisher.publishRawMessage(event.getPayload());
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(Instant.now());
        log.info("Published outbox event with ID: {}", event.getId());

      } catch (SnsException e) {
        log.error("SNS publish failed — id: {}", event.getId(), e);
        event.setStatus(OutboxStatus.FAILED);
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastRetriedAt(Instant.now());
      } finally {
        try {
          outboxEventRepository.save(event);
        } catch (Exception dbEx) {
          log.error("Failed to update outbox event status — id: {}",
              event.getId(), dbEx);
          // continue to next event regardless
        }
      }
    }
  }
  @Scheduled(fixedDelay = 10000)  // retry every 10 seconds
  @Transactional
  public void retryFailedEvents() {
    List<OutboxEvent> retryableEvents =
        outboxEventRepository.findByStatusAndRetryCountLessThan(OutboxStatus.FAILED, 3);
    for (OutboxEvent event : retryableEvents) {
      try {
        snsMessagePublisher.publishRawMessage(event.getPayload());
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setLastRetriedAt(Instant.now());
        log.info("Retried outbox event — id: {}", event.getId());
      } catch (Exception e) {
        log.error("Retry failed — id: {}", event.getId(), e);
        event.setRetryCount(event.getRetryCount() + 1);
        if (event.getRetryCount() >= 3) {
          event.setStatus(OutboxStatus.DEAD);
          log.error("Outbox event marked as DEAD — id: {}", event.getId());
        }
      } finally {
        try {
          outboxEventRepository.save(event);
        } catch (Exception dbEx) {
          log.error("Failed to save outbox event — id: {}", event.getId(), dbEx);
        }
      }
    }
  }
}
