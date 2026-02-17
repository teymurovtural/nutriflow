package com.nutriflow.dto.request;

import com.nutriflow.enums.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "Status qeyd olunmalıdır")
    private DeliveryStatus status;

    @Size(max = 255, message = "Qeyd 255 simvolu keçməməlidir")
    private String catererNote;
}