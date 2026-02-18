package com.nutriflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryFailureRequest {

    @NotNull(message = "Delivery ID is required!")
    private Long deliveryId;

    @NotBlank(message = "Failure reason is required!")
    private String failureReason;

    private String note;
}