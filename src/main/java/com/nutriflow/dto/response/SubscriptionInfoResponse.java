package com.nutriflow.dto.response;

import com.nutriflow.enums.SubscriptionStatus;
import com.nutriflow.enums.UserStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionInfoResponse {

    private Long subscriptionId;
    private Long userId;

    // User info
    private String firstName;
    private String lastName;
    private String email;
    private UserStatus userStatus;

    // Subscription info
    private String planName;
    private Double price;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private long daysRemaining;
    private long monthsRemaining;
    private boolean isActive;
}