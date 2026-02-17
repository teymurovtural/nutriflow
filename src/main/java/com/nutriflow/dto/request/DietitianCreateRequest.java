package com.nutriflow.dto.request;

import com.nutriflow.enums.Role;
import com.nutriflow.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietitianCreateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    private String specialization;
    private Role role;
}
