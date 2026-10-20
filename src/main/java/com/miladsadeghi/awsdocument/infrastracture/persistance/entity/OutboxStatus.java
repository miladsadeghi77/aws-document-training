package com.miladsadeghi.awsdocument.infrastracture.persistance.entity;

public enum OutboxStatus {
  PENDING,
  PUBLISHED,
  FAILED,
  DEAD    // exceeded max retries — needs manual intervention

}