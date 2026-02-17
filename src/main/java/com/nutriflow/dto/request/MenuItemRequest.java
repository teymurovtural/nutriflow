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

    @NotNull(message = "Gün (1-31) mütləq qeyd olunmalıdır")
    @Min(value = 1, message = "Gün minimum 1 ola bilər")
    @Max(value = 31, message = "Gün maksimum 31 ola bilər")
    private Integer day;

    @NotNull(message = "Yemək növü (MealType) mütləqdir")
    private MealType mealType;

    @NotBlank(message = "Yemək təsviri boş ola bilməz")
    @Size(min = 5, max = 1000, message = "Təsvir 5-1000 simvol aralığında olmalıdır")
    private String description;

    @PositiveOrZero(message = "Kalori mənfi ola bilməz")
    private Integer calories;

    @PositiveOrZero(message = "Protein mənfi ola bilməz")
    private Double protein;

    @PositiveOrZero(message = "Karbohidrat mənfi ola bilməz")
    private Double carbs;

    @PositiveOrZero(message = "Yağ mənfi ola bilməz")
    private Double fats;
}