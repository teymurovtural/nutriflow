package com.nutriflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CatererProfileUpdateRequest {

    @Size(min = 2, max = 100, message = "Company name must be between 2-100 characters")
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is not in a valid format")
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}