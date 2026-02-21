package com.nutriflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReassignDietitianRequest {

    @NotNull(message = "New dietitian ID is required")
    private Long newDietitianId;
}