package com.nutriflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Integer day;
    private String mealType;
    private String description;
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fats;
}