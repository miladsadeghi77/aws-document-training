package com.miladsadeghi.awsdocument.api.Controller;

import com.miladsadeghi.awsdocument.domain.service.DocumentService;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
  private final DocumentService documentService;

  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @PostMapping("/upload")
  public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
      throws IOException {
    String key = documentService.storeDocument(file);
    return ResponseEntity.ok("Uploaded successfully. Key: " + key);
  }
}
