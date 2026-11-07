package com.miladsadeghi.awsdocument.infrastracture;

import com.miladsadeghi.awsdocument.domain.model.ProcessingStep;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

@Repository
@Slf4j
public class ProcessingStepRepository {
  private final DynamoDbClient dynamoDbClient;
  private final String tableName;

  public ProcessingStepRepository(DynamoDbClient dynamoDbClient,
      @Value("${aws.dynamodb.table-name}") String tableName) {
    this.dynamoDbClient = dynamoDbClient;
    this.tableName = tableName;
  }

  /** Returns true if saved, false if this step already existed. */
  public boolean saveIfNotExists(ProcessingStep step) {
    Map<String, AttributeValue> item = Map.of(
        "documentKey", AttributeValue.fromS(step.documentKey()),
        "step", AttributeValue.fromS(step.step()),
        "status", AttributeValue.fromS(step.status()),
        "updatedAt", AttributeValue.fromS(step.updatedAt()));

    PutItemRequest request = PutItemRequest.builder()
        .tableName(tableName)
        .item(item)
        .conditionExpression("attribute_not_exists(documentKey)")
        .build();

    try {
      dynamoDbClient.putItem(request);
      return true;
    } catch (ConditionalCheckFailedException e) {
      log.info("Step already exists — key: {}, step: {}", step.documentKey(), step.step());
      return false;
    }
  }
  public List<ProcessingStep> findByDocumentKey(String documentKey) {
    QueryRequest request = QueryRequest.builder()
        .tableName(tableName)
        .keyConditionExpression("documentKey = :k")
        .expressionAttributeValues(Map.of(":k", AttributeValue.fromS(documentKey)))
        .build();

    return dynamoDbClient.query(request).items().stream()
        .map(i -> new ProcessingStep(
            i.get("documentKey").s(),
            i.get("step").s(),
            i.get("status").s(),
            i.get("updatedAt").s()))
        .toList();
  }

}
