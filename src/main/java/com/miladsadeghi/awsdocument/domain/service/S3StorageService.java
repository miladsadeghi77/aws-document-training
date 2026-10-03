package com.miladsadeghi.awsdocument.domain.service;

import com.miladsadeghi.awsdocument.domain.exception.DocumentNotFoundException;
import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import com.miladsadeghi.awsdocument.domain.model.FileDownloadResult;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@Slf4j
public class S3StorageService {

  private final S3Client s3Client;

  private final String bucket;
  private final S3Presigner s3Presigner;

  public S3StorageService(S3Client s3Client, @Value("${aws.s3.bucket}") String bucket,
      S3Presigner s3Presigner) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.s3Presigner = s3Presigner;
  }

  public String upload(String key, byte[] content, String contentType) {
    log.info("Uploading object to S3 — bucket: {}, key: {}", bucket, key);

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .build();
    try {
      s3Client.putObject(request, RequestBody.fromBytes(content));

      log.info("Upload successful — key: {}", key);
      return key;

    } catch (S3Exception e) {
      log.error("Failed to upload object to S3 — bucket: {}, key: {}, error: {}",
          bucket, key, e.getMessage());
      throw new StorageException("Failed to upload file to storage: ", e);
    }
  }

  public FileDownloadResult download(String key) {
    log.info("Downloading object from S3 <UNK> bucket: {}, key: {}", bucket, key);
    GetObjectRequest request = GetObjectRequest.builder()
        .key(key)
        .bucket(bucket)
        .build();

    try {
      ResponseInputStream<GetObjectResponse> responseStream = s3Client.getObject(request,
          ResponseTransformer.toInputStream());
      String contentType = responseStream.response().contentType();
      String filename = extractFilename(key);
      log.info("Download successful key: {}, size: {} bytes", key,
          responseStream.response().contentLength());
      return new FileDownloadResult(
          responseStream,
          contentType,
          filename
      );
    } catch (NoSuchKeyException e) {
      log.warn("Object not found in S3 — bucket: {}, key: {}", bucket, key);
      throw new DocumentNotFoundException("Document not found: " + key);
    } catch (S3Exception e) {
      log.error("Failed to download object from S3 — bucket: {}, key: {}, error: {}",
          bucket, key, e.getMessage());
      throw new StorageException("Failed to download file from storage", e);
    }
  }

  public String generateDownloadUrl(String key, Duration expiration) {
    log.info("Generating presigned download URL — bucket: {}, key: {}, expiration: {}", bucket, key,
        expiration);
    GetObjectPresignRequest request = GetObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .getObjectRequest(r -> r.bucket(bucket).key(key))
        .build();

    PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(request);
    String url = presignedRequest.url().toString();
    log.info("Presigned download URL generated — key: {}", key);
    return url;
  }

  public String generateUploadUrl(String key, Duration expiration) {
    log.info("Generating presigned upload URL — bucket: {}, key: {}, expiration: {}",
        bucket, key, expiration);
    PutObjectPresignRequest request = PutObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .putObjectRequest(r -> r.bucket(bucket).key(key))
        .build();
    PresignedPutObjectRequest presignedPutRequest = s3Presigner.presignPutObject(request);
    String url = presignedPutRequest.url().toString();
    log.info("Presigned upload URL generated — key: {}", key);
    return url;
  }

  private String extractFilename(String key) {
    return key.substring(key.lastIndexOf("/") + 1);
  }
}
