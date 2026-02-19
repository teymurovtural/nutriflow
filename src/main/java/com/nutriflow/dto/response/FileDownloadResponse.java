package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Getter
@Builder
public class FileDownloadResponse {
    private final byte[] data;
    private final String fileName;
    private final String contentType;

    public ResponseEntity<byte[]> toResponseEntity() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(data);
    }
}