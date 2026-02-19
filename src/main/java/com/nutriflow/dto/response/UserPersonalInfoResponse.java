package com.nutriflow.dto.response;

import com.nutriflow.enums.GoalType;
import com.nutriflow.enums.UserStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPersonalInfoResponse {

    // User info
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserStatus userStatus;
    private boolean isEmailVerified;

    // Health data
    private Double height;
    private Double weight;
    private Double bmi;
    private GoalType goal;
    private String restrictions;
    private String notes;

    // Medical files
    private List<MedicalFileResponse> medicalFiles;
}