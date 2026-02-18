package com.nutriflow.dto.response;

import com.nutriflow.enums.Role;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {

    private Long id;
    private LocalDateTime createdAt;
    private Role role;
    private Long actorId;
    private String action;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String details;

}