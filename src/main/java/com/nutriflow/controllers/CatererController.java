package com.nutriflow.controllers;

import com.nutriflow.dto.request.CatererProfileUpdateRequest;
import com.nutriflow.dto.request.DeliveryFailureRequest;
import com.nutriflow.dto.request.DeliveryStatusUpdateRequest;
import com.nutriflow.dto.response.CatererResponse;
import com.nutriflow.dto.response.CatererStatsResponse;
import com.nutriflow.dto.response.DeliveryDetailResponse;
import com.nutriflow.services.CatererService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Caterer (Kuryer/Aşpaz) paneli üçün əməliyyatları idarə edən controller.
 */
@RestController
@RequestMapping("/api/v1/caterer")
@PreAuthorize("hasRole('CATERER')")
@RequiredArgsConstructor
public class CatererController {

    private final CatererService catererService;

    /**
     * Dashboard üçün statistik məlumatları (cəmi, yolda, çatdırılan və s.) qaytarır.
     */
    @GetMapping("/stats")
    public ResponseEntity<CatererStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(catererService.getDashboardStats());
    }

    /**
     * Müəyyən edilmiş tarixə (default olaraq bugün) görə çatdırılma siyahısını qaytarır.
     */
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryDetailResponse>> getDailyDeliveries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(catererService.getDailyDeliveries(name, district, date));
    }

    /**
     * Sifarişin statusunu yeniləyir və kuryerin qeydini bazaya yazır.
     */
    @PatchMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) { // DTO validasiyası aktivdir

        // Status və kuryer qeydi service-ə ötürülür
        catererService.updateDeliveryStatus(deliveryId, request.getStatus(), request.getCatererNote());
        return ResponseEntity.noContent().build();
    }

    /**
     * Kuryerin profil məlumatlarını (ad, telefon, ünvan) gətirir.
     */
    @GetMapping("/profile")
    public ResponseEntity<CatererResponse> getProfile() {
        return ResponseEntity.ok(catererService.getProfile());
    }

    /**
     * Kuryerin profil məlumatlarını yeniləyir.
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody CatererProfileUpdateRequest request) {
        String message = catererService.updateProfile(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Müştəri üçün təxmini çatdırılma vaxtını təyin edir.
     */
    @PutMapping("/deliveries/{id}/estimate")
    public ResponseEntity<String> updateEstimate(
            @PathVariable Long id,
            @RequestParam String time) {
        catererService.updateEstimatedTime(id, time);
        return ResponseEntity.ok("Təxmini çatdırılma vaxtı qeyd olundu: " + time);
    }

    @PatchMapping("/deliveries/failed")
    public ResponseEntity<String> markDeliveryAsFailed(
            @Valid @RequestBody DeliveryFailureRequest request) {
        catererService.markDeliveryAsFailed(request);
        return ResponseEntity.ok("Çatdırılma uğursuz kimi işarələndi.");
    }
}