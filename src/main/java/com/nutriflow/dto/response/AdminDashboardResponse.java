package com.nutriflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalDietitians;
    private long totalCaterers;
    private long activeSubscriptions;
    private Double totalRevenue;
    private long totalDeliveries;
    private long successfulDeliveries;
    private long failedDeliveries;
    private long pendingMenus;
    private long approvedMenus;
    private long rejectedMenus;
    private long newUsersThisMonth;
    private Map<String, Double> chartData;
}