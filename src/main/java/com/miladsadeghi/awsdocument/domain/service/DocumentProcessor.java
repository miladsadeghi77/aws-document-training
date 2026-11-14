package com.miladsadeghi.awsdocument.domain.service;

import com.miladsadeghi.awsdocument.domain.model.DocumentUploadedEvent;
import com.miladsadeghi.awsdocument.domain.model.ProcessingStep;
import com.miladsadeghi.awsdocument.infrastracture.ProcessingStepRepository;
import java.time.Instant;
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
  private final ProcessingStepRepository processingStepRepository;

  public DocumentProcessor(S3Client s3Client,
      @Value("${aws.s3.bucket}") String bucket, ProcessingStepRepository processingStepRepository) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.processingStepRepository = processingStepRepository;
  }


  public void process(DocumentUploadedEvent event) {
    log.info("=== Processing document ===");
    log.info("Key:        {}", event.documentKey());
    log.info("Bucket:     {}", event.bucket());
    log.info("Uploaded:   {}", event.uploadedAt());

    boolean alreadyDone = processingStepRepository.findByDocumentKey(event.documentKey()).stream()
        .anyMatch(s -> "METADATA".equals(s.step()) && "DONE".equals(s.status()));

    if (alreadyDone) {
      log.info("Document is already done");
      return;
    }

    HeadObjectResponse metadata = fetchMetadata(event.documentKey());

    log.info("Size:        {} bytes", metadata.contentLength());
    log.info("ContentType: {}", metadata.contentType());
    log.info("=== Processing complete ===");

    ProcessingStep processingStep = new ProcessingStep(event.documentKey(), "METADATA", "DONE", Instant.now().toString());
    boolean saved = processingStepRepository.saveIfNotExists(processingStep);
    if (!saved){
      log.info("Step was saved by another delivery — key: {}", event.documentKey());
    }

  }

  private HeadObjectResponse fetchMetadata(String key) {
    HeadObjectRequest request = HeadObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    return s3Client.headObject(request);
  }
}
