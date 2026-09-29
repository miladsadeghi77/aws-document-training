package com.miladsadeghi.awsdocument.domain.service;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3StorageService {
  private final S3Client s3Client;

  private final String bucket;

  public S3StorageService(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
    this.s3Client = s3Client;
    this.bucket = bucket;
  }

  public String upload(String key, byte[] content, String contentType) {
    log.info("Uploading object to S3 — bucket: {}, key: {}", bucket, key);

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .build();
    try{
      s3Client.putObject(request, RequestBody.fromBytes(content));
      log.info("Upload successful — key: {}", key);
      return key;

    }catch (Exception e){
      log.error("Failed to upload object to S3 — bucket: {}, key: {}, error: {}",
          bucket, key, e.getMessage());
      throw new StorageException("Failed to upload file to storage: " +  e.getMessage());
    }
  }
}
