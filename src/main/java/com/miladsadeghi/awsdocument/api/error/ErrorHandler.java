package com.miladsadeghi.awsdocument.api.error;

import com.miladsadeghi.awsdocument.common.logging.CorrelationContext;
import com.miladsadeghi.awsdocument.domain.exception.DocumentNotFoundException;
import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<ErrorResponse> handleStorageException(StorageException e) {

    log.error("Storage failure: {} ", e.getMessage(), e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Storage operation failed. Please try again later.",
            MDC.get(CorrelationContext.MDC_KEY)));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(IllegalArgumentException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(e.getMessage() ,MDC.get(CorrelationContext.MDC_KEY)) );
  }

  @ExceptionHandler(DocumentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(DocumentNotFoundException e) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse(e.getMessage(),MDC.get(CorrelationContext.MDC_KEY)));
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<ErrorResponse> handleGenericIOException(Exception e) {
    log.error("Content of file isn't correct: {}", e.getMessage(), e);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse("Content of file isn't correct",MDC.get(CorrelationContext.MDC_KEY)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An unexpected error occurred.",MDC.get(CorrelationContext.MDC_KEY)));

  }
}