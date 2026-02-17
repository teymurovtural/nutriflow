package com.nutriflow.dto.response;

import com.nutriflow.enums.Role;
import com.nutriflow.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse extends BaseAuthResponse {
    private String email;
    private UserStatus status;
    private Role role;
}