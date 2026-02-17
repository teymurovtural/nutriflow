package com.nutriflow.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentAdminResponse {
    private Long id;
    private Double amount;
    private String currency;
    private String status;
    private LocalDateTime paymentDate;
    private String transactionId;
    private String userEmail;
    private Long subscriptionId;
}