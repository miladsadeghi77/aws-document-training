package com.miladsadeghi.awsdocument.domain.model;

public record ProcessingStep(
    String documentKey,
    String step,
    String status,
    String updatedAt
) {}
