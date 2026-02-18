package com.nutriflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nutriflow.enums.CatererStatus;
import com.nutriflow.enums.OperationStatus;
import com.nutriflow.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminActionResponse {

    private String message;
    private Long targetId;
    private OperationStatus operationStatus;
    private UserStatus userStatus;
    private CatererStatus catererStatus;
    private Boolean dietitianActive;
    private LocalDateTime timestamp;

}
