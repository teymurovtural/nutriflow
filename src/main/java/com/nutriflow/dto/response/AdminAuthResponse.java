package com.nutriflow.dto.response;

import com.nutriflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAuthResponse extends BaseAuthResponse {

    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private boolean isActive;

}