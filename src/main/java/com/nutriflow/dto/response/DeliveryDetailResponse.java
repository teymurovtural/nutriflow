package com.nutriflow.dto.response;


import com.nutriflow.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DeliveryDetailResponse {

    private Long deliveryId;
    private String clientFullName;
    private LocalDate deliveryDate;
    private Integer dayNumber;
    private String phone;
    private String fullAddress;
    private String district;
    private String deliveryNotes;
    private DeliveryStatus status;
    private String estimatedTime;
    private List<MealInfoResponse> meals;

}
