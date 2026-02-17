package com.nutriflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryFailureRequest {

    @NotNull(message = "Çatdırılma ID-si məcburidir!")
    private Long deliveryId;

    @NotBlank(message = "Uğursuzluq səbəbi məcburidir!")
    private String failureReason;

    private String note;
}