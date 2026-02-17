package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicalFileResponse {
    private Long id;
    private String fileName;
    private String fileUrl;
}