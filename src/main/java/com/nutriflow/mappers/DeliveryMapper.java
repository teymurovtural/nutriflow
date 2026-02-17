package com.nutriflow.mappers;

import com.nutriflow.dto.response.DeliveryDetailResponse;
import com.nutriflow.dto.response.MealInfoResponse;
import com.nutriflow.entities.DeliveryEntity;
import com.nutriflow.entities.MenuItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DeliveryMapper {

    public DeliveryDetailResponse toDetailResponse(DeliveryEntity entity, List<MenuItemEntity> dailyItems) {
        if (entity == null) return null;

        return DeliveryDetailResponse.builder()
                .deliveryId(entity.getId())
                // --- YENİ ƏLAVƏLƏR ---
                .deliveryDate(entity.getDate())
                .dayNumber(entity.getDate() != null ? entity.getDate().getDayOfMonth() : null)
                // ---------------------
                .clientFullName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName())
                .phone(entity.getUser().getPhoneNumber())
                .fullAddress(entity.getAddress().getAddressDetails())
                .district(entity.getAddress().getDistrict())
                .deliveryNotes(entity.getDeliveryNotes() != null && !entity.getDeliveryNotes().isBlank()
                        ? entity.getDeliveryNotes()
                        : entity.getAddress().getDeliveryNotes())
                .status(entity.getStatus())
                .meals(mapToMealInfo(dailyItems))
                .estimatedTime(entity.getEstimatedDeliveryTime())
                .build();
    }

    private List<MealInfoResponse> mapToMealInfo(List<MenuItemEntity> items) {
        if (items == null) return List.of();
        return items.stream()
                .map(item -> MealInfoResponse.builder()
                        .type(item.getMealType())
                        .description(item.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public DeliveryDetailResponse toDetailResponse(DeliveryEntity entity) {
        return toDetailResponse(entity, null);
    }
}