package com.miladsadeghi.awsdocument.infrastracture.sns.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Slf4j
@Service
public class SnsMessagePublisher {

  private final SnsClient snsClient;
  private final String topicArn;

  public SnsMessagePublisher(SnsClient snsClient,
      @Value("${aws.sns.topic-arn}") String topicArn) {
    this.snsClient = snsClient;
    this.topicArn = topicArn;
  }

  public void publishRawMessage(String payload) throws SnsException {
      PublishRequest request = PublishRequest.builder()
          .topicArn(topicArn)
          .message(payload)
          .subject("DocumentUploaded")
          .build();

      PublishResponse response = snsClient.publish(request);
      log.info("Raw message published to SNS — messageId: {}", response.messageId());
  }
}
