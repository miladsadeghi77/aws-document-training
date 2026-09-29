package com.miladsadeghi.awsdocument.domain.service;

import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service

public class DocumentService {

  private final S3StorageService s3StorageService;

  public DocumentService(S3StorageService s3StorageService) {
    this.s3StorageService = s3StorageService;
  }

  public String storeDocument(MultipartFile file) throws IOException {
    String key = generateKey(file.getOriginalFilename());
    return s3StorageService.upload(key, file.getBytes(), file.getContentType());
  }
  private String generateKey(String originalFilename) {
    return "documents/" + UUID.randomUUID() + "/" + originalFilename;
  }
}
