package com.nutriflow.dto.request;

import com.nutriflow.enums.MealType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemRequest {

    @NotNull(message = "Day (1-31) must be specified")
    @Min(value = 1, message = "Day minimum value is 1")
    @Max(value = 31, message = "Day maximum value is 31")
    private Integer day;

    @NotNull(message = "Meal type (MealType) is required")
    private MealType mealType;

    @NotBlank(message = "Meal description cannot be blank")
    @Size(min = 5, max = 1000, message = "Description must be between 5-1000 characters")
    private String description;

    @PositiveOrZero(message = "Calories cannot be negative")
    private Integer calories;

    @PositiveOrZero(message = "Protein cannot be negative")
    private Double protein;

    @PositiveOrZero(message = "Carbohydrates cannot be negative")
    private Double carbs;

    @PositiveOrZero(message = "Fats cannot be negative")
    private Double fats;
}