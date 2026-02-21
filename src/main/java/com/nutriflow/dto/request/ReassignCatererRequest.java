package com.nutriflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReassignCatererRequest {

    @NotNull(message = "New caterer ID is required")
    private Long newCatererId;
}