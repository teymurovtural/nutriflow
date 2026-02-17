package com.nutriflow.services;

import com.nutriflow.dto.request.HealthDataRequest;
import com.nutriflow.dto.response.HealthDataResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface HealthProfileService {
    HealthDataResponse submitCompleteProfile(String email, HealthDataRequest request, List<MultipartFile> files) throws IOException;
}