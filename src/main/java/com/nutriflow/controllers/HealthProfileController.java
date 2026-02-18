package com.nutriflow.controllers;

import com.nutriflow.dto.request.HealthDataRequest;
import com.nutriflow.dto.response.HealthDataResponse;
import com.nutriflow.services.HealthProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/health-profile")
@RequiredArgsConstructor
@Slf4j
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    @PostMapping(value = "/submit", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<HealthDataResponse> submitProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("data") @Valid HealthDataRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {

        log.info("=== CONTROLLER: Health profile submit endpoint called ===");
        log.info("User email: {}", userDetails.getUsername());
        log.info("Files parameter null? {}", files == null);

        if (files != null) {
            log.info("File count: {}", files.size());
            for (int i = 0; i < files.size(); i++) {
                log.info("File #{}: {}, size: {}", i, files.get(i).getOriginalFilename(), files.get(i).getSize());
            }
        } else {
            log.warn("!!! CONTROLLER: FILES CAME AS NULL !!!");
        }

        String email = userDetails.getUsername();
        HealthDataResponse response = healthProfileService.submitCompleteProfile(email, request, files);

        return ResponseEntity.ok(response);
    }
}