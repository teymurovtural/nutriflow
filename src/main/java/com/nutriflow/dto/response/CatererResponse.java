package com.nutriflow.dto.response;

import com.nutriflow.enums.CatererStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CatererResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private CatererStatus status;

}