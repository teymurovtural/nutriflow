package com.nutriflow.dto.response;

import com.nutriflow.enums.MenuStatus;
import com.nutriflow.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class UserDashboardResponse {

    private String planName;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate nextRenewalDate;
    private MenuStatus menuStatus;
    private String dietitianFullName;
    private Long completedDeliveries;
    private Integer totalDays;
    private Double progressPercentage;

}