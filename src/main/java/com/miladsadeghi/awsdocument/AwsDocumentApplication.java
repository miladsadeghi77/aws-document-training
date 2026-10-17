package com.miladsadeghi.awsdocument;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AwsDocumentApplication {

  public static void main(String[] args) {
    SpringApplication.run(AwsDocumentApplication.class, args);
  }

}
