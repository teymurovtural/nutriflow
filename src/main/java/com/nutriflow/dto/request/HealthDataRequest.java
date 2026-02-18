package com.nutriflow.dto.request;

import com.nutriflow.enums.GoalType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HealthDataRequest {

    @NotNull(message = "Height is required")
    @Min(value = 50, message = "Height cannot be less than 50 cm")
    @Max(value = 250, message = "Height cannot be more than 250 cm")
    private Double height;

    @NotNull(message = "Weight is required")
    @Min(value = 20, message = "Weight cannot be less than 20 kg")
    @Max(value = 300, message = "Weight cannot be more than 300 kg")
    private Double weight;

    @NotNull(message = "Goal is required")
    private GoalType goal;

    private String restrictions; // Allergies, vegetarianism, etc.
    private String notes;

    @NotBlank(message = "Full address must be provided")
    private String addressDetails;

    @NotBlank(message = "City is required")
    private String city;

    private String district;
    private String deliveryNotes;
}