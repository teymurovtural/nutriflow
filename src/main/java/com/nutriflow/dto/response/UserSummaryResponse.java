package com.nutriflow.dto.response;

import com.nutriflow.enums.GoalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSummaryResponse {

    private Long userId;
    private String firstName;
    private String lastName;
    private String dietitianFullName;
    private String catererFullName;
    private String email;
    private String phoneNumber;
    private String status;
    private GoalType goal;
    private Double height;
    private Double weight;
    private String restrictions;
    private String notes;
}