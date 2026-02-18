package com.nutriflow.dto.request;

import com.nutriflow.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "Status must be specified")
    private DeliveryStatus status;

    @Size(max = 255, message = "Note must not exceed 255 characters")
    private String catererNote;
}