package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietitianDashboardResponse {
    private long totalPatients;    // Bütün pasiyentləri
    private long pendingMenus;     // Statusu DATA_SUBMITTED olanlar
    private long activeMenus;      // Statusu ACTIVE olanlar
}