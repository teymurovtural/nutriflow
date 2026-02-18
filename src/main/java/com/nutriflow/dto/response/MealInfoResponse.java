package com.nutriflow.dto.response;


import com.nutriflow.enums.MealType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MealInfoResponse {

    private MealType type;
    private String description;

}
