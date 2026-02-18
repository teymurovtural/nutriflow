package com.nutriflow.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Year must be specified")
    @Min(value = 2024, message = "Cannot create a menu for past years")
    private Integer year;

    @NotNull(message = "Month must be specified")
    @Min(1) @Max(12)
    private Integer month;

    private String dietaryNotes;

    @NotEmpty(message = "Menu cannot be empty. At least one meal plan must be provided")
    @Valid
    private List<MenuItemRequest> items;
}