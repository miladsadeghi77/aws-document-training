package com.miladsadeghi.awsdocument.api.filter;

import static com.miladsadeghi.awsdocument.common.logging.CorrelationContext.HEADER;
import static com.miladsadeghi.awsdocument.common.logging.CorrelationContext.MDC_KEY;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9-]{1,64}");

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String correlationId = request.getHeader(HEADER);
    if (correlationId == null || !VALID_ID.matcher(correlationId).matches()) {
      correlationId = UUID.randomUUID().toString();
    }
    MDC.put(MDC_KEY, correlationId);
    response.setHeader(HEADER, correlationId);
    try {
      filterChain.doFilter(request, response);
    }finally {
      MDC.remove(MDC_KEY);
    }
  }
}
