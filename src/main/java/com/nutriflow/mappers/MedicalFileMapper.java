package com.nutriflow.mappers;

import com.nutriflow.dto.response.FileDownloadResponse;
import com.nutriflow.entities.MedicalFileEntity;
import org.springframework.stereotype.Component;

@Component
public class MedicalFileMapper {

    public FileDownloadResponse toFileDownloadResponse(MedicalFileEntity entity, byte[] data) {
        return FileDownloadResponse.builder()
                .data(data)
                .fileName(entity.getFileName() != null
                        ? entity.getFileName()
                        : "medical_file_" + entity.getId())
                .contentType(entity.getFileType() != null
                        ? entity.getFileType()
                        : "application/octet-stream")
                .build();
    }
}
