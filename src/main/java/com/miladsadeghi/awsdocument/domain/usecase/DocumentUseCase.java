package com.miladsadeghi.awsdocument.domain.usecase;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import com.miladsadeghi.awsdocument.domain.service.S3StorageService;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentUseCase {

  private final S3StorageService s3StorageService;

  public DocumentUseCase(S3StorageService s3StorageService) {
    this.s3StorageService = s3StorageService;
  }

  public String storeDocument(MultipartFile file) {
    validateFile(file);

    String key = generateKey(file.getOriginalFilename());
    try {
      return s3StorageService.upload(key, file.getBytes(), file.getContentType());
    } catch (IOException e) {
      throw new StorageException("Failed to read file content", e);
    }
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
