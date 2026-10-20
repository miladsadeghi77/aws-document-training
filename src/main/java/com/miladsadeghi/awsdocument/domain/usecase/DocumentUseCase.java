package com.miladsadeghi.awsdocument.domain.usecase;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import com.miladsadeghi.awsdocument.domain.model.DocumentUploadedEvent;
import com.miladsadeghi.awsdocument.domain.model.FileDownloadResult;
import com.miladsadeghi.awsdocument.domain.service.S3StorageService;
import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.Document;
import com.miladsadeghi.awsdocument.infrastracture.persistance.entity.OutboxEvent;
import com.miladsadeghi.awsdocument.infrastracture.persistance.repository.DocumentRepository;
import com.miladsadeghi.awsdocument.infrastracture.persistance.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class DocumentUseCase {

  private final S3StorageService s3StorageService;
  private final DocumentRepository documentRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;
  private final String bucket;

  public DocumentUseCase(
      S3StorageService s3StorageService,
      DocumentRepository documentRepository,
      OutboxEventRepository outboxEventRepository,
      ObjectMapper objectMapper,
      @Value("${aws.s3.bucket}")  String bucket) {

    this.s3StorageService = s3StorageService;
    this.documentRepository = documentRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.bucket = bucket;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public String storeDocument(MultipartFile file) {
    validateFile(file);

    String key = generateKey(file.getOriginalFilename());
    try {
      // Write 1: S3 upload
      String uploadedKey = s3StorageService.upload(key, file.getBytes(), file.getContentType());

      // Write 2 + 3: single DB transaction
      Document document = new Document(uploadedKey, bucket, file.getOriginalFilename());
      documentRepository.save(document);

      DocumentUploadedEvent event = new DocumentUploadedEvent(uploadedKey, bucket,
          Instant.now().toString());

      String payload = objectMapper.writeValueAsString(event); // Serialize to JSON
      outboxEventRepository.save(new OutboxEvent("DocumentUploaded", payload));
      log.info("Document saved and outbox event created — key: {}", key);
      return key;

    } catch (IOException e) {
      throw new StorageException("Failed to read file content", e);
    }
  }

  public FileDownloadResult downloadDocument(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Document key must not be empty");
    }
    return s3StorageService.download(key);
  }
  public String getDownloadUrlDocument(String key) {
    if (key == null || key.isBlank()) {
      throw new IllegalArgumentException("Document key must not be empty");
    }
    return s3StorageService.generateDownloadUrl(key , Duration.ofMinutes(10));
  }

  public String getUploadUrlDocument(String filename){
    if (filename == null || filename.isBlank()) {
      throw new IllegalArgumentException("Filename must not be empty");
    }
    String key = generateKey(filename);
    return s3StorageService.generateUploadUrl(key, Duration.ofMinutes(10));
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File must not be null or empty");
    }
    if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
      throw new IllegalArgumentException("File must have a valid name");
    }
  }

  private String generateKey(String originalFilename) {
    return "documents/" + UUID.randomUUID() + "/" + originalFilename;
  }
}
