package com.nutriflow.mappers;

import com.nutriflow.dto.request.CatererCreateRequest;
import com.nutriflow.dto.response.AdminActionResponse;
import com.nutriflow.dto.response.CatererResponse;
import com.nutriflow.dto.response.CatererStatsResponse;
import com.nutriflow.entities.CatererEntity;
import com.nutriflow.enums.CatererStatus;
import com.nutriflow.helpers.DeliveryHelper;
import org.springframework.stereotype.Component;

/**
 * Mapping between Caterer Entity and DTOs.
 * Enhanced version - response building in mapper.
 */
@Component
public class CatererMapper {

    /**
     * Creates CatererEntity from CatererCreateRequest.
     */
    public CatererEntity toEntity(CatererCreateRequest request) {
        if (request == null) return null;

        return CatererEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(CatererStatus.ACTIVE)
                .build();
    }

    /**
     * Creates CatererResponse from CatererEntity.
     */
    public CatererResponse toResponse(CatererEntity caterer) {
        if (caterer == null) return null;

        return CatererResponse.builder()
                .id(caterer.getId())
                .name(caterer.getName())
                .email(caterer.getEmail())
                .phone(caterer.getPhone())
                .address(caterer.getAddress())
                .status(caterer.getStatus())
                .build();
    }

    /**
     * Creates CatererStatsResponse from CatererStatsData.
     * Maps the data structure coming from Helper to response.
     */
    public CatererStatsResponse toStatsResponse(DeliveryHelper.CatererStatsData stats) {
        if (stats == null) return null;

        return CatererStatsResponse.builder()
                .totalOrders(stats.getTotalOrders())
                .inProgress(stats.getInProgress())
                .ready(stats.getReady())
                .onTheWay(stats.getOnTheWay())
                .delivered(stats.getDelivered())
                .failed(stats.getFailed())
                .build();
    }

    /**
     * Updates entity fields with data from the request.
     */
    public void updateEntityFromRequest(CatererEntity entity, CatererCreateRequest request) {
        if (request == null || entity == null) return;

        entity.setName(request.getName());
        entity.setPhone(request.getPhone());
        entity.setAddress(request.getAddress());
    }

    /**
     * Formats the operation result into standard AdminActionResponse format.
     */
    public AdminActionResponse toAdminActionResponse(CatererEntity entity, String message) {
        if (entity == null) return null;

        return AdminActionResponse.builder()
                .message(message)
                .targetId(entity.getId())
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .catererStatus(entity.getStatus())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}