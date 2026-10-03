package com.miladsadeghi.awsdocument.api.Controller;

import com.miladsadeghi.awsdocument.api.dto.DocumentUploadResponse;
import com.miladsadeghi.awsdocument.domain.model.FileDownloadResult;
import com.miladsadeghi.awsdocument.domain.usecase.DocumentUseCase;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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

  @GetMapping("/download/**")
  public ResponseEntity<StreamingResponseBody> download(HttpServletRequest request) {
    String key = extractKey(request);
    FileDownloadResult result = documentUseCase.downloadDocument(key);

    StreamingResponseBody streamingResponseBody = out -> {
      try(InputStream in = result.inputStream()) {
        in.transferTo(out);
      }
    };

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + result.filename() + "\"")
        .contentType(MediaType.parseMediaType(result.contentType()))
        .body(streamingResponseBody);
  }

  @GetMapping("/presigned-download-url")
  public ResponseEntity<String> getDownloadUrl(@RequestParam String key) {
    String url = documentUseCase.getDownloadUrlDocument(key);
    return ResponseEntity.ok(url);
  }

  @GetMapping("/presigned-upload-url")
  public ResponseEntity<String> getUploadUrl(@RequestParam String filename) {
    String url = documentUseCase.getUploadUrlDocument(filename);
    return ResponseEntity.ok(url);
  }
  private String extractKey(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.substring(path.indexOf("/download/") + "/download/".length());
  }
}
