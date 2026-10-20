package com.miladsadeghi.awsdocument.domain.usecase;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import com.miladsadeghi.awsdocument.domain.model.FileDownloadResult;
import com.miladsadeghi.awsdocument.domain.service.S3StorageService;
import com.miladsadeghi.awsdocument.infrastracture.sns.publisher.SnsMessagePublisher;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentUseCase {

  private final S3StorageService s3StorageService;
  private final SnsMessagePublisher snsMessagePublisher;
  private final String bucket;

  public DocumentUseCase(S3StorageService s3StorageService, SnsMessagePublisher snsMessagePublisher,
      @Value("${aws.s3.bucket}")  String bucket) {
    this.s3StorageService = s3StorageService;
    this.snsMessagePublisher = snsMessagePublisher;
    this.bucket = bucket;
  }

  public String storeDocument(MultipartFile file) {
    validateFile(file);

    String key = generateKey(file.getOriginalFilename());
    try {
      String uploadedKey = s3StorageService.upload(key, file.getBytes(), file.getContentType());
      snsMessagePublisher.publishDocumentUploaded(uploadedKey, bucket);
      return uploadedKey;
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
