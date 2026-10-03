package com.miladsadeghi.awsdocument.domain.exception;

public class DocumentNotFoundException extends RuntimeException {

  public DocumentNotFoundException(String message,Throwable cause) {
    super(message,cause);
  }

  public DocumentNotFoundException(String message) {
    super(message);
  }
}
