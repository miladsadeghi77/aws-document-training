package com.miladsadeghi.awsdocument.common.logging;

public final class CorrelationContext {
  public static final String HEADER = "X-Correlation-Id";
  public static final String MDC_KEY = "correlationId";

  private CorrelationContext() {}
}
