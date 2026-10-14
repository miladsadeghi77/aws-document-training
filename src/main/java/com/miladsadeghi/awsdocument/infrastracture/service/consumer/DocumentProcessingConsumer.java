package com.miladsadeghi.awsdocument.infrastracture.service.consumer;


import com.miladsadeghi.awsdocument.domain.model.DocumentUploadedEvent;
import com.miladsadeghi.awsdocument.domain.service.DocumentProcessor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class DocumentProcessingConsumer {

  private final SqsClient sqsClient;
  private final String queueUrl;
  private final ObjectMapper objectMapper;
  private final DocumentProcessor documentProcessor;

  public DocumentProcessingConsumer(SqsClient sqsClient,
      ObjectMapper objectMapper,
      DocumentProcessor documentProcessor,
      @Value("${aws.sqs.queue-url}") String queueUrl) {
    this.sqsClient = sqsClient;
    this.objectMapper = objectMapper;
    this.documentProcessor = documentProcessor;
    this.queueUrl = queueUrl;
  }

  @Scheduled(fixedRate = 5000)
  public void listen() {
    ReceiveMessageRequest request = ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .maxNumberOfMessages(10)
        .waitTimeSeconds(20) // long polling
        .build();

    List<Message> messages = sqsClient.receiveMessage(request).messages();

    for (Message message : messages) {
      processMessage(message);
    }
  }

  private void processMessage(Message message) {
    log.info("Processing message — messageId: {}", message.messageId());
    try {
      DocumentUploadedEvent event = objectMapper.readValue(message.body(),
          DocumentUploadedEvent.class);

      documentProcessor.process(event);
      deleteMessage(message.receiptHandle());
      log.info("Message processed and deleted — messageId: {}", message.messageId());

    } catch (JacksonException e) {
      log.error("Failed to deserialize message — messageId: {}", message.messageId());
      deleteMessage(message.receiptHandle());

    }catch (Exception e) {
      log.error("Failed to process message — messageId: {}, error: {}",
          message.messageId(), e.getMessage());
      // do NOT delete — let visibility timeout expire → SQS retries
    }
  }

  private void deleteMessage(String receiptHandle) {
    DeleteMessageRequest request = DeleteMessageRequest.builder()
        .queueUrl(queueUrl)
        .receiptHandle(receiptHandle)
        .build();
    sqsClient.deleteMessage(request);
  }
}