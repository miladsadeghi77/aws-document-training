package com.miladsadeghi.awsdocument.infrastracture.service.consumer;


import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Service
@Slf4j
public class DocumentProcessingConsumer {

  private final SqsClient sqsClient;
  private final String queueUrl;

  public DocumentProcessingConsumer(SqsClient sqsClient,
      @Value("${aws.sqs.queue-url}") String queueUrl) {
    this.sqsClient = sqsClient;
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
      log.info("Message body: {}", message.body());

      // TODO: deserialize and process the event

      deleteMessage(message.receiptHandle());
      log.info("Message processed and deleted — messageId: {}", message.messageId());

    } catch (Exception e) {
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