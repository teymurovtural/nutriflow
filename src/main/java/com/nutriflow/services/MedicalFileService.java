package com.nutriflow.services;

import com.nutriflow.dto.response.FileDownloadResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface MedicalFileService {
        FileDownloadResponse getMedicalFileForDownload(Long fileId, UserDetails userDetails);
}
