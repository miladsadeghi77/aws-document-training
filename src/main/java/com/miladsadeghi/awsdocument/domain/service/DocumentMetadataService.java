package com.miladsadeghi.awsdocument.domain.service;

import com.miladsadeghi.awsdocument.domain.exception.DocumentNotFoundException;
import com.miladsadeghi.awsdocument.domain.model.DocumentMetadata;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class DocumentMetadataService {

  private static final Duration TTL = Duration.ofMinutes(5);
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;
  private final S3Client s3Client;
  private final String bucket;

  public DocumentMetadataService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
      S3Client s3Client, @Value("${aws.s3.bucket}") String bucket) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.s3Client = s3Client;
    this.bucket = bucket;
  }

  public DocumentMetadata getMetadata(String documentKey) {
    String cacheKey = "doc-metadata:" + documentKey;
    Optional<DocumentMetadata> cached = readFromCache(cacheKey);
    if (cached.isPresent()) {
      log.info("Cache HIT — {}", cacheKey);
      return cached.get();
    }

    log.info("Cache MISS — {}", cacheKey);
    DocumentMetadata metadata = fetchFromS3(documentKey);   // S3 errors SHOULD propagate
    writeToCache(cacheKey, metadata);
    return metadata;
  }

  private Optional<DocumentMetadata> readFromCache(String cacheKey) {
    try {
      String cached = redisTemplate.opsForValue().get(cacheKey);
      if (cached == null) {
        return Optional.empty();
      }
      return Optional.of(objectMapper.readValue(cached, DocumentMetadata.class));
    } catch (DataAccessException | JacksonException e) {
      log.warn("Cache read failed — key: {}, reason: {}", cacheKey, e.getMessage());
      return Optional.empty();
    }
  }

  private void writeToCache(String cacheKey, DocumentMetadata metadata) {
    try {
      redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(metadata), TTL);
    } catch (DataAccessException | JacksonException e) {
      log.warn("Cache write failed — key: {}, reason: {}", cacheKey, e.getMessage());
    }
  }

  private DocumentMetadata fetchFromS3(String documentKey) {
    try {
      HeadObjectResponse head = s3Client.headObject(
          HeadObjectRequest.builder().bucket(bucket).key(documentKey).build());
      return new DocumentMetadata(documentKey, head.contentLength(),
          head.contentType());

    } catch (NoSuchKeyException e) {
      log.warn("Object not found in S3 — bucket: {}, key: {}", bucket, documentKey);
      throw new DocumentNotFoundException("Document not found: " + documentKey);
    }

  }
}
