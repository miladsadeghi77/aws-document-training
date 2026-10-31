package com.miladsadeghi.awsdocument.domain.model;

public record DocumentUploadedEvent (
    String documentKey,
    String bucket,
    String uploadedAt,
    String correlationId
) {}

