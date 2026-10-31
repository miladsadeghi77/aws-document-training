package com.miladsadeghi.awsdocument.infrastracture.persistance.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String eventType;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OutboxStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  private Instant publishedAt;
  @Column(nullable = false)
  private int retryCount = 0;

  private Instant lastRetriedAt;
  private String correlationId;

  public OutboxEvent(String eventType, String payload, String correlationId) {
    this.eventType = eventType;
    this.payload = payload;
    this.status = OutboxStatus.PENDING;
    this.createdAt = Instant.now();
    this.correlationId = correlationId;
  }
}