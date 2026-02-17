package com.nutriflow.dto.response;

import com.nutriflow.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthDataResponse {
    private String message;
    private String userEmail;
    private UserStatus newStatus;
}