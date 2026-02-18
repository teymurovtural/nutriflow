package com.nutriflow.controllers;

import com.nutriflow.dto.request.*;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.security.SecurityUser;
import com.nutriflow.services.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // --- 1. DASHBOARD & STATISTICS ---
    @GetMapping("/dashboard/stats")
    public ResponseEntity<AdminDashboardResponse> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.getDashboardStatistics(start, end, currentUser));
    }

    // --- 2. PROFILE MANAGEMENT ---
    @PutMapping("/profile")
    public ResponseEntity<AdminActionResponse> updateProfile(
            @Valid @RequestBody AdminProfileUpdateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.updateAdminProfile(request, currentUser));
    }

    // --- 3. CREATE ---
    @PostMapping("/users")
    public ResponseEntity<AdminActionResponse> createUser(
            @Valid @RequestBody RegisterRequestForAdmin request, // Changed here
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request, currentUser));
    }

    @PostMapping("/dietitians")
    public ResponseEntity<AdminActionResponse> createDietitian(
            @Valid @RequestBody DietitianCreateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createDietitian(request, currentUser));
    }

    @PostMapping("/caterers")
    public ResponseEntity<AdminActionResponse> createCaterer(
            @Valid @RequestBody CatererCreateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCaterer(request, currentUser));
    }

    @PostMapping("/sub-admins")
    public ResponseEntity<AdminActionResponse> createSubAdmin(
            @Valid @RequestBody AdminCreateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createSubAdmin(request, currentUser));
    }

    // --- 4. LISTING ---
    @GetMapping("/users")
    public ResponseEntity<Page<UserSummaryResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllUsers(pageable));
    }

    @GetMapping("/dietitians")
    public ResponseEntity<Page<DietitianProfileResponse>> getAllDietitians(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllDietitians(pageable));
    }

    @GetMapping("/caterers")
    public ResponseEntity<Page<CatererResponse>> getAllCaterers(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllCaterers(pageable));
    }

    @GetMapping("/sub-admins")
    public ResponseEntity<Page<AdminSummaryResponse>> getAllSubAdmins(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllSubAdmins(pageable));
    }

    // --- 5. SEARCH ---
    @GetMapping("/users/search")
    public ResponseEntity<Page<UserSummaryResponse>> searchUsers(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(adminService.searchUsers(query, pageable));
    }

    @GetMapping("/dietitians/search")
    public ResponseEntity<Page<DietitianProfileResponse>> searchDietitians(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(adminService.searchDietitians(query, pageable));
    }

    // --- 6. ASSIGNMENT ---
    @PostMapping("/users/{userId}/assign-dietitian/{dietitianId}")
    public ResponseEntity<AdminActionResponse> assignDietitian(
            @PathVariable Long userId,
            @PathVariable Long dietitianId,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.assignDietitianToUser(userId, dietitianId, currentUser));
    }

    @PostMapping("/users/{userId}/assign-caterer/{catererId}")
    public ResponseEntity<AdminActionResponse> assignCaterer(
            @PathVariable Long userId,
            @PathVariable Long catererId,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.assignCatererToUser(userId, catererId, currentUser));
    }

    @GetMapping("/users/pending-assignments")
    public ResponseEntity<PendingAssignmentResponse> getPendingAssignments() {
        return ResponseEntity.ok(adminService.getPendingDietitianAssignments());
    }

    @GetMapping("/users/pending-caterer-assignments")
    public ResponseEntity<PendingAssignmentResponse> getPendingCatererAssignments() {
        return ResponseEntity.ok(adminService.getPendingCatererAssignments());
    }

    // --- 7. STATUS TOGGLE ---
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<AdminActionResponse> toggleUserStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id, currentUser));
    }

    @PatchMapping("/dietitians/{id}/toggle-status")
    public ResponseEntity<AdminActionResponse> toggleDietitianStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.toggleDietitianStatus(id, currentUser));
    }

    @PatchMapping("/caterers/{id}/toggle-status")
    public ResponseEntity<AdminActionResponse> toggleCatererStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.toggleCatererStatus(id, currentUser));
    }

    @PatchMapping("/sub-admins/{id}/toggle-status")
    public ResponseEntity<AdminActionResponse> toggleSubAdminStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.toggleSubAdminStatus(id, currentUser));
    }

    // --- 8. DELETE ---
    @DeleteMapping("/users/{id}")
    public ResponseEntity<AdminActionResponse> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.deleteUser(id, currentUser));
    }

    @DeleteMapping("/dietitians/{id}")
    public ResponseEntity<AdminActionResponse> deleteDietitian(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.deleteDietitian(id, currentUser));
    }

    @DeleteMapping("/caterers/{id}")
    public ResponseEntity<AdminActionResponse> deleteCaterer(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.deleteCaterer(id, currentUser));
    }

    @DeleteMapping("/sub-admins/{id}")
    public ResponseEntity<AdminActionResponse> deleteSubAdmin(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.deleteSubAdmin(id, currentUser));
    }

    // --- 9. OTHER ---

    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentAdminResponse>> getAllPayments(Pageable pageable) {
        // Now returning PaymentAdminResponse instead of PaymentEntity
        return ResponseEntity.ok(adminService.getAllPayments(pageable));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentAdminResponse> getPaymentDetails(@PathVariable Long id) {
        // Also switched to DTO for single payment details
        return ResponseEntity.ok(adminService.getPaymentDetails(id));
    }

    @GetMapping("/logs")
    public ResponseEntity<PagedModel<ActivityLogResponse>> getActivityLogs(Pageable pageable) {
        Page<ActivityLogResponse> page = adminService.getAllActivityLogs(pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }
}