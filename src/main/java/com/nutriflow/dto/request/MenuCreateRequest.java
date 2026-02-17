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

    @NotNull(message = "İstifadəçi ID-si mütləqdir")
    private Long userId;

    @NotNull(message = "İl qeyd olunmalıdır")
    @Min(value = 2024, message = "Keçmiş illərə menyu yazıla bilməz")
    private Integer year;

    @NotNull(message = "Ay qeyd olunmalıdır")
    @Min(1) @Max(12)
    private Integer month;

    private String dietaryNotes;

    @NotEmpty(message = "Menyu boş ola bilməz. Ən azı bir yemək planı daxil edilməlidir")
    @Valid
    private List<MenuItemRequest> items;
}