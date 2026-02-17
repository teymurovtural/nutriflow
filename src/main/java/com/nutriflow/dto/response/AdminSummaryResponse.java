package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminSummaryResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;
    private boolean isSuperAdmin;
}
