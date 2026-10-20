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
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String s3Key;

  @Column(nullable = false)
  private String bucket;

  @Column(nullable = false)
  private String originalFilename;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DocumentStatus status;

  @Column(nullable = false)
  private Instant uploadedAt;

  public Document(String s3Key, String bucket,
      String originalFilename) {
    this.s3Key = s3Key;
    this.bucket = bucket;
    this.originalFilename = originalFilename;
    this.status = DocumentStatus.UPLOADED;
    this.uploadedAt = Instant.now();
  }
}