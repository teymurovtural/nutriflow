package com.nutriflow.controllers;

import com.nutriflow.dto.request.DietitianUpdateRequest;
import com.nutriflow.dto.request.MenuCreateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.enums.MealType;
import com.nutriflow.services.DietitianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dietitian")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DIETITIAN')")
@Slf4j
public class DietitianController {

    private final DietitianService dietitianService;

    @GetMapping("/my-users")
    public ResponseEntity<List<UserSummaryResponse>> getMyUsers(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(dietitianService.getMyAssignedUsers(email));
    }

    /**
     * @Valid must be used when creating a new menu.
     * Meal lists and days inside MenuCreateRequest are validated.
     */
    @PostMapping("/create-menu")
    public ResponseEntity<String> createMenu(
            Authentication authentication,
            @Valid @RequestBody MenuCreateRequest request) { // @Valid added

        String email = authentication.getName();
        dietitianService.createMonthlyMenu(email, request);

        return ResponseEntity.ok("Menu created successfully.");
    }

    /**
     * Name, phone, etc. entered when updating the dietitian profile are validated.
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(
            Principal principal,
            @Valid @RequestBody DietitianUpdateRequest request) { // @Valid added

        String message = dietitianService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/profile")
    public ResponseEntity<DietitianProfileResponse> getProfile(Principal principal) {
        DietitianProfileResponse profile = dietitianService.getProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DietitianDashboardResponse> getDashboardStats(Principal principal) {
        return ResponseEntity.ok(dietitianService.getDashboardStats(principal.getName()));
    }

    @GetMapping("/patients/urgent")
    public ResponseEntity<List<UserSummaryResponse>> getUrgentPatients(Principal principal) {
        return ResponseEntity.ok(dietitianService.getUrgentPatients(principal.getName()));
    }

    @GetMapping("/patient/{userId}/profile")
    public ResponseEntity<PatientMedicalProfileResponse> getPatientProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(dietitianService.getPatientMedicalProfile(userId));
    }

    @GetMapping("/menu/{userId}")
    public ResponseEntity<MenuResponse> getMonthlyMenu(
            @PathVariable Long userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {

        log.info("Monthly menu query: UserID: {}, Date: {}/{}", userId, month, year);
        MenuResponse menu = dietitianService.getMonthlyMenu(userId, year, month);
        return ResponseEntity.ok(menu);
    }

    @PatchMapping("/batch/{batchId}/submit")
    public ResponseEntity<String> submitMenu(@PathVariable Long batchId) {
        log.info("Menu batch being submitted to user. BatchID: {}", batchId);
        String result = dietitianService.submitMenu(batchId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/patients/search")
    public ResponseEntity<List<UserSummaryResponse>> searchPatients(
            Principal principal,
            @RequestParam String query) {
        return ResponseEntity.ok(dietitianService.searchMyPatients(principal.getName(), query));
    }

    @GetMapping("/batch/{batchId}/rejection-reason")
    public ResponseEntity<MenuRejectionDetailResponse> getRejectionReason(@PathVariable Long batchId) {
        return ResponseEntity.ok(dietitianService.getMenuRejectionReason(batchId));
    }

    @GetMapping("/patient/file/{fileId}")
    public ResponseEntity<MedicalFileDetailResponse> getFileUrl(@PathVariable Long fileId) {
        return ResponseEntity.ok(dietitianService.getAnalysisFileUrl(fileId));
    }

    @GetMapping("/batch/{batchId}/items")
    public ResponseEntity<MenuResponse> getBatchItems(@PathVariable Long batchId) {
        return ResponseEntity.ok(dietitianService.getBatchDetails(batchId));
    }

    /**
     * New data validity must be checked when editing an existing menu.
     */
    @PutMapping("/batch/{batchId}/update")
    public ResponseEntity<String> updateMenu(
            @PathVariable Long batchId,
            @Valid @RequestBody MenuCreateRequest request) { // @Valid added
        dietitianService.updateMenu(batchId, request);
        return ResponseEntity.ok("Menu updated successfully and ready for resubmission.");
    }

    @DeleteMapping("/batch/{batchId}/delete-content")
    public ResponseEntity<String> deleteContent(
            @PathVariable Long batchId,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) MealType mealType) {

        log.info("Delete operation: Batch: {}, Day: {}, Meal: {}", batchId, day, mealType);
        String message = dietitianService.deleteMenuContent(batchId, day, mealType);
        return ResponseEntity.ok(message);
    }
}