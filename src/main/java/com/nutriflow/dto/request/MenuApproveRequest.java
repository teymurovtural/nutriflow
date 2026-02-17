package com.nutriflow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuApproveRequest {

    private Long batchId;

    @Size(max = 500, message = "Çatdırılma qeydi 500 simvolu keçməməlidir")
    private String deliveryNotes;
}