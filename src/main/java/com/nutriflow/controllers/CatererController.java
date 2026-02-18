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
 * Controller managing operations for the Caterer (Courier/Chef) panel.
 */
@RestController
@RequestMapping("/api/v1/caterer")
@PreAuthorize("hasRole('CATERER')")
@RequiredArgsConstructor
public class CatererController {

    private final CatererService catererService;

    /**
     * Returns statistical data for the dashboard (total, on the way, delivered, etc.).
     */
    @GetMapping("/stats")
    public ResponseEntity<CatererStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(catererService.getDashboardStats());
    }

    /**
     * Returns the delivery list for a specified date (default: today).
     */
    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryDetailResponse>> getDailyDeliveries(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(catererService.getDailyDeliveries(name, district, date));
    }

    /**
     * Updates the order status and saves the courier's note to the database.
     */
    @PatchMapping("/deliveries/{deliveryId}/status")
    public ResponseEntity<Void> updateDeliveryStatus(
            @PathVariable Long deliveryId,
            @Valid @RequestBody DeliveryStatusUpdateRequest request) { // DTO validation is active

        // Status and courier note are passed to the service
        catererService.updateDeliveryStatus(deliveryId, request.getStatus(), request.getCatererNote());
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the courier's profile information (name, phone, address).
     */
    @GetMapping("/profile")
    public ResponseEntity<CatererResponse> getProfile() {
        return ResponseEntity.ok(catererService.getProfile());
    }

    /**
     * Updates the courier's profile information.
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody CatererProfileUpdateRequest request) {
        String message = catererService.updateProfile(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Sets the estimated delivery time for the customer.
     */
    @PutMapping("/deliveries/{id}/estimate")
    public ResponseEntity<String> updateEstimate(
            @PathVariable Long id,
            @RequestParam String time) {
        catererService.updateEstimatedTime(id, time);
        return ResponseEntity.ok("Estimated delivery time recorded: " + time);
    }

    @PatchMapping("/deliveries/failed")
    public ResponseEntity<String> markDeliveryAsFailed(
            @Valid @RequestBody DeliveryFailureRequest request) {
        catererService.markDeliveryAsFailed(request);
        return ResponseEntity.ok("Delivery marked as failed.");
    }
}