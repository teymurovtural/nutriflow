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
    // Dashboard statistika məlumatları
    CatererStatsResponse getDashboardStats();

    // Bugünkü çatdırılmalar (Axtarış və Filter ilə)
    List<DeliveryDetailResponse> getDailyDeliveries(String name, String district, LocalDate date);

    // Çatdırılma statusunun yenilənməsi
    void updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus, String note);

    // Profil məlumatlarını görmək
    CatererResponse getProfile();

    // Profil məlumatlarını yeniləmək (Ad, Telefon, Ünvan)
    String updateProfile(CatererProfileUpdateRequest request);

    void updateEstimatedTime(Long deliveryId, String estimatedTime);

    void markDeliveryAsFailed(DeliveryFailureRequest request);


}