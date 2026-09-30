package com.miladsadeghi.awsdocument.api.Controller;

import com.miladsadeghi.awsdocument.api.dto.DocumentUploadResponse;
import com.miladsadeghi.awsdocument.domain.usecase.DocumentUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

  private final DocumentUseCase documentUseCase;

  public DocumentController(DocumentUseCase documentUseCase) {
    this.documentUseCase = documentUseCase;
  }

  @PostMapping("/upload")
  public ResponseEntity<DocumentUploadResponse> upload(@RequestParam("file") MultipartFile file) {
    String key = documentUseCase.storeDocument(file);

    return ResponseEntity.ok(new DocumentUploadResponse(key, "Uploaded successfully"));
  }
}
