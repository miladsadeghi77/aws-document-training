package com.miladsadeghi.awsdocument.infrastracture.sns.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SnsEnvelope(
    @JsonProperty("Type") String type,
    @JsonProperty("TopicArn") String topicArn,
    @JsonProperty("Message") String message,
    @JsonProperty("Timestamp") String timestamp
) {}
