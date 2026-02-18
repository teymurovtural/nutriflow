package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicalFileDetailResponse {

    private String userFullName;
    private String fileName;
    private String fileUrl;

}
