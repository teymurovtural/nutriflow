package com.nutriflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DietitianUpdateRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2-50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2-50 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 100, message = "Specialization must not exceed 100 characters")
    private String specialization;
}