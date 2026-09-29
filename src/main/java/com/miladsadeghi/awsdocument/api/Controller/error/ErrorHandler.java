package com.miladsadeghi.awsdocument.api.Controller.error;

import com.miladsadeghi.awsdocument.domain.exception.StorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@Slf4j
@RestControllerAdvice
public class ErrorHandler {

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<String> handleException(final Exception exception) {
    HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    return new ResponseEntity<>(exception.getMessage(), httpStatus);
  }
  @ExceptionHandler(value = {StorageException.class})
  public ResponseEntity<String> handleException(final StorageException exception) {
    HttpStatus httpStatus = HttpStatus.NOT_FOUND;
    return new ResponseEntity<>(exception.getMessage(), httpStatus);
  }
}
