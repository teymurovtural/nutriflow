package com.nutriflow.dto.response;

import com.nutriflow.enums.MenuStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MenuBatchAdminResponse {
    private Long batchId;
    private Long menuId;
    private String userFullName;
    private String dietitianFullName;
    private String catererFullName;
    private MenuStatus status;
    private String rejectionReason;
    private Integer year;
    private Integer month;
    private int totalItems;
    private LocalDateTime createdAt;
}
