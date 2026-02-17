package com.nutriflow.dto.request;

import com.nutriflow.enums.GoalType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HealthDataRequest {

    @NotNull(message = "Boy mütləqdir")
    @Min(value = 50, message = "Boy 50 sm-dən az ola bilməz")
    @Max(value = 250, message = "Boy 250 sm-dən çox ola bilməz")
    private Double height;

    @NotNull(message = "Çəki mütləqdir")
    @Min(value = 20, message = "Çəki 20 kq-dan az ola bilməz")
    @Max(value = 300, message = "Çəki 300 kq-dan çox ola bilməz")
    private Double weight;

    @NotNull(message = "Məqsəd mütləqdir")
    private GoalType goal;

    private String restrictions; // Allergiyalar, vegeterianlıq və s.
    private String notes;

    @NotBlank(message = "Ünvan tam qeyd olunmalıdır")
    private String addressDetails;

    @NotBlank(message = "Şəhər mütləqdir")
    private String city;

    private String district;
    private String deliveryNotes;
}