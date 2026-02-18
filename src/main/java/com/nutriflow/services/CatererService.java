package com.nutriflow.services;

import com.nutriflow.dto.request.CatererProfileUpdateRequest;
import com.nutriflow.dto.request.DeliveryFailureRequest;
import com.nutriflow.dto.response.CatererResponse;
import com.nutriflow.dto.response.CatererStatsResponse;
import com.nutriflow.dto.response.DeliveryDetailResponse;
import com.nutriflow.enums.DeliveryStatus;

import java.time.LocalDate;
import java.util.List;

public interface CatererService {
    // Dashboard statistics
    CatererStatsResponse getDashboardStats();

    // Today's deliveries (with Search and Filter)
    List<DeliveryDetailResponse> getDailyDeliveries(String name, String district, LocalDate date);

    // Update delivery status
    void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, String note);

    // View profile information
    CatererResponse getProfile();

    // Update profile information (Name, Phone, Address)
    String updateProfile(CatererProfileUpdateRequest request);

    void updateEstimatedTime(Long deliveryId, String estimatedTime);

    void markDeliveryAsFailed(DeliveryFailureRequest request);
}