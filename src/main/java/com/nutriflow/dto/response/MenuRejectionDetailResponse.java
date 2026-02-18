package com.nutriflow.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuRejectionDetailResponse {

    private Long batchId;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String phoneNumber;
    private String rejectionReason;

}