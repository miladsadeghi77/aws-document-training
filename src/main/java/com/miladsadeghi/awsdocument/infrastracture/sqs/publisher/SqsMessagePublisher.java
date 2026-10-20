package com.miladsadeghi.awsdocument.infrastracture.sqs.publisher;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import com.miladsadeghi.awsdocument.domain.model.DocumentUploadedEvent;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import software.amazon.awssdk.services.sqs.model.SqsException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class SqsMessagePublisher {
  private final SqsClient sqsClient;
  private final ObjectMapper objectMapper;
  private final String queueUrl;

  public SqsMessagePublisher(SqsClient sqsClient, ObjectMapper objectMapper,
      @Value("${aws.sqs.queue-url}") String queueUrl) {

    this.sqsClient = sqsClient;
    this.objectMapper = objectMapper;
    this.queueUrl = queueUrl;
  }

  public void publishDocumentUploaded(String documentKey, String bucket) {
    try {
      DocumentUploadedEvent event = new DocumentUploadedEvent(
          documentKey,
          bucket,
          Instant.now().toString()
      );

      String messageBody = objectMapper.writeValueAsString(event);

      SendMessageRequest request = SendMessageRequest.builder()
          .queueUrl(queueUrl)
          .messageBody(messageBody)
          .build();

      SendMessageResponse response = sqsClient.sendMessage( request);

      log.info("Message published to SQS — messageId: {}, key: {}",
          response.messageId(), documentKey);

    } catch (SqsException e) {
      log.error("Failed to publish message to SQS — key: {}", documentKey, e);
      throw new StorageException("Failed to publish document event", e);
    }
  }
}
