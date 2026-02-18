package com.nutriflow.dto.request;

import com.nutriflow.enums.GoalType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    // --- Personal information ---
    @Size(min = 2, max = 50, message = "First name must be between 2-50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2-50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number is not in a valid format (e.g: +994501234567)")
    private String phoneNumber;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    // --- Address information ---
    @Size(max = 100, message = "City name is too long")
    private String city;

    @Size(max = 100, message = "District name is too long")
    private String district;

    @Size(max = 255, message = "Address details must not exceed 255 characters")
    private String addressDetails;

    @Size(max = 500, message = "Delivery notes are too long")
    private String deliveryNotes;

    // --- Health information ---
    @Positive(message = "Weight must be a positive number")
    @Max(value = 500, message = "Weight value is unrealistic")
    private Double weight;

    @Positive(message = "Height must be a positive number")
    @Max(value = 300, message = "Height value is unrealistic")
    private Double height;

    private GoalType goal; // Enum type, @NotNull is usually sufficient (if required)

    @Size(max = 1000, message = "Restrictions note must not exceed 1000 characters")
    private String restrictions;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;
}