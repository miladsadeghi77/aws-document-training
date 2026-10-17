package com.miladsadeghi.awsdocument.infrastracture.service.publisher;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import com.miladsadeghi.awsdocument.domain.model.DocumentUploadedEvent;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class SnsMessagePublisher {

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;
  private final String topicArn;

  public SnsMessagePublisher(SnsClient snsClient, ObjectMapper objectMapper,
      @Value("${aws.sns.topic-arn}") String topicArn) {
    this.snsClient = snsClient;
    this.objectMapper = objectMapper;
    this.topicArn = topicArn;
  }

  public void publishDocumentUploaded(String documentKey, String bucket) {
    try {
      DocumentUploadedEvent event = new DocumentUploadedEvent(
          documentKey,
          bucket,
          Instant.now().toString()
      );

      String message = objectMapper.writeValueAsString(event);

      PublishRequest request = PublishRequest.builder()
          .topicArn(topicArn)
          .message(message)
          .subject("DocumentUploaded")
          .build();

      PublishResponse response = snsClient.publish(request);

      log.info("Message published to SNS — messageId: {}, key: {}",
          response.messageId(), documentKey);


    } catch (SnsException e) {
      log.error("Failed to publish to SNS — key: {}", documentKey, e);
      throw new StorageException("Failed to publish document event", e);
    }
  }
}
