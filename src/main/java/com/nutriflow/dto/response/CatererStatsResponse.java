package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatererStatsResponse {

    private long totalOrders;
    private long inProgress;
    private long ready;
    private long onTheWay;
    private long delivered;
    private long failed;

}
