package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DietitianProfileResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String specialization;
    private String phone;
    private String role;
    private long totalPatients;

}