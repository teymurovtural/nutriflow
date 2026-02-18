package com.nutriflow.dto.response;

import com.nutriflow.enums.CatererStatus;
import com.nutriflow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CatererAuthResponse extends BaseAuthResponse {

    private String email;
    private String companyName;
    private String phone;
    private CatererStatus status;
    private Role role;

}