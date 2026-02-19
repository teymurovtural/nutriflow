package com.nutriflow.controllers;

import com.nutriflow.services.MedicalFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medical-files")
@RequiredArgsConstructor
public class MedicalFileController {

    private final MedicalFileService medicalFileService;

    @GetMapping("/{fileId}/download")
    @PreAuthorize("hasAnyRole('USER', 'DIETITIAN', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> downloadMedicalFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long fileId) {
        return medicalFileService.getMedicalFileForDownload(fileId, userDetails)
                .toResponseEntity();
    }
}
