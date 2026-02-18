package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietitianDashboardResponse {

    private long totalPatients;
    private long pendingMenus;
    private long activeMenus;

}