package com.nutriflow.mappers;

import com.nutriflow.dto.request.MenuItemRequest;
import com.nutriflow.dto.response.MenuItemResponse;
import com.nutriflow.dto.response.MenuResponse;
import com.nutriflow.entities.MenuBatchEntity;
import com.nutriflow.entities.MenuEntity;
import com.nutriflow.entities.MenuItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuMapper {

    /**
     * Converts incoming Request data to MenuItemEntity.
     * Note: The meal is now linked to a Batch (Package), not directly to a Menu.
     */
    public MenuItemEntity toMenuItemEntity(MenuItemRequest request, MenuBatchEntity batch) {
        return MenuItemEntity.builder()
                .batch(batch)
                .day(request.getDay())
                .mealType(request.getMealType())
                .description(request.getDescription())
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbs(request.getCarbs())
                .fats(request.getFats())
                .build();
    }

    /**
     * Converts MenuEntity and its associated Batch into a single Response DTO.
     */
    public MenuResponse toResponseDTO(MenuEntity menu, MenuBatchEntity batch) {
        if (menu == null || batch == null) return null;

        return MenuResponse.builder()
                .menuId(menu.getId())
                .batchId(batch.getId()) // DTO now has batchId
                .year(menu.getYear())
                .month(menu.getMonth())
                .dietaryNotes(menu.getDietaryNotes())
                .status(batch.getStatus().name()) // Status now comes from Batch
                .items(batch.getItems().stream()
                        .map(this::toMenuItemResponseDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public MenuItemResponse toMenuItemResponseDTO(MenuItemEntity entity) {
        if (entity == null) return null;

        return MenuItemResponse.builder()
                .day(entity.getDay())
                .mealType(entity.getMealType().name())
                .description(entity.getDescription())
                .calories(entity.getCalories())
                .protein(entity.getProtein())
                .carbs(entity.getCarbs())
                .fats(entity.getFats())
                .build();
    }
}