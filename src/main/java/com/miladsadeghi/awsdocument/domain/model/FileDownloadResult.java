package com.miladsadeghi.awsdocument.domain.model;

public record FileDownloadResult(
    byte[] content,
    String contentType,
    String filename
) {
}
