package com.miladsadeghi.awsdocument.api.error;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class ErrorHandler {

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<String> handleStorageException(StorageException e) {
    log.error("Storage failure: {}", e.getMessage(), e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("Storage operation failed. Please try again later.");
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleValidationException(IllegalArgumentException e) {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(e.getMessage());
  }

  @ExceptionHandler(IOException.class)
  public ResponseEntity<String> handleGenericIOException(Exception e) {
    log.error("Content of file isn't correct: {}", e.getMessage(), e);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body("Content of file isn't correct");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGenericException(Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An unexpected error occurred.");
  }
}