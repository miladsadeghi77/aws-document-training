package com.miladsadeghi.awsdocument.domain.service;

import com.miladsadeghi.awsdocument.domain.model.DocumentUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

@Service
@Slf4j
public class DocumentProcessor {
  private final S3Client s3Client;
  private final String bucket;

  public DocumentProcessor(S3Client s3Client,
      @Value("${aws.s3.bucket}") String bucket) {
    this.s3Client = s3Client;
    this.bucket = bucket;
  }


  public void process(DocumentUploadedEvent event) {
    log.info("=== Processing document ===");
    log.info("Key:        {}", event.documentKey());
    log.info("Bucket:     {}", event.bucket());
    log.info("Uploaded:   {}", event.uploadedAt());

    HeadObjectResponse metadata = fetchMetadata(event.documentKey());

    log.info("Size:        {} bytes", metadata.contentLength());
    log.info("ContentType: {}", metadata.contentType());
    log.info("=== Processing complete ===");
  }

  private HeadObjectResponse fetchMetadata(String key) {
    HeadObjectRequest request = HeadObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    return s3Client.headObject(request);
  }
}
