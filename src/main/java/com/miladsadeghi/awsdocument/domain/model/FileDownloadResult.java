package com.miladsadeghi.awsdocument.domain.model;

import java.io.InputStream;

public record FileDownloadResult(
    InputStream inputStream,
    String contentType,
    String filename
) {
}
