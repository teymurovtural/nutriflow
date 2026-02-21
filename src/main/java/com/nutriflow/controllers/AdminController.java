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
            @Valid @RequestBody RegisterRequestForAdmin request,
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

    // --- 5. GET BY ID ---
    @GetMapping("/users/{id}")
    public ResponseEntity<UserSummaryResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @GetMapping("/dietitians/{id}")
    public ResponseEntity<DietitianProfileResponse> getDietitianById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getDietitianById(id));
    }

    @GetMapping("/caterers/{id}")
    public ResponseEntity<CatererResponse> getCatererById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getCatererById(id));
    }

    // --- 6. SEARCH ---
    @GetMapping("/users/search")
    public ResponseEntity<Page<UserSummaryResponse>> searchUsers(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(adminService.searchUsers(query, pageable));
    }

    @GetMapping("/dietitians/search")
    public ResponseEntity<Page<DietitianProfileResponse>> searchDietitians(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(adminService.searchDietitians(query, pageable));
    }

    @GetMapping("/menus")
    public ResponseEntity<Page<MenuBatchAdminResponse>> getAllMenuBatches(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllMenuBatches(pageable));
    }

    @GetMapping("/menus/{batchId}")
    public ResponseEntity<MenuBatchAdminResponse> getMenuBatchById(@PathVariable Long batchId) {
        return ResponseEntity.ok(adminService.getMenuBatchById(batchId));
    }

    // --- 7. ASSIGNMENT ---
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

    // --- 8. STATUS TOGGLE ---
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

    // --- 9. DELETE ---
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

    // --- 10. PAYMENTS ---
    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentAdminResponse>> getAllPayments(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllPayments(pageable));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentAdminResponse> getPaymentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPaymentDetails(id));
    }

    // --- 11. SUBSCRIPTION ---
    @GetMapping("/users/{userId}/subscription/info")
    public ResponseEntity<SubscriptionInfoResponse> getUserSubscriptionInfo(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserSubscriptionInfo(userId));
    }

    // --- 12. LOGS ---
    @GetMapping("/logs")
    public ResponseEntity<PagedModel<ActivityLogResponse>> getActivityLogs(Pageable pageable) {
        Page<ActivityLogResponse> page = adminService.getAllActivityLogs(pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    // --- 13. EDIT ---
    @PutMapping("/users/{id}")
    public ResponseEntity<AdminActionResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserProfileUpdateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.updateUser(id, request, currentUser));
    }

    @PutMapping("/dietitians/{id}")
    public ResponseEntity<AdminActionResponse> updateDietitian(
            @PathVariable Long id,
            @Valid @RequestBody DietitianUpdateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.updateDietitian(id, request, currentUser));
    }

    @PutMapping("/caterers/{id}")
    public ResponseEntity<AdminActionResponse> updateCaterer(
            @PathVariable Long id,
            @Valid @RequestBody CatererProfileUpdateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.updateCaterer(id, request, currentUser));
    }

    @PutMapping("/sub-admins/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AdminActionResponse> updateSubAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminProfileUpdateRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.updateSubAdmin(id, request, currentUser));
    }

    // --- 14. REASSIGN ---
    @PatchMapping("/users/{userId}/reassign-dietitian")
    public ResponseEntity<AdminActionResponse> reassignDietitian(
            @PathVariable Long userId,
            @Valid @RequestBody ReassignDietitianRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.reassignDietitian(userId, request, currentUser));
    }

    @PatchMapping("/users/{userId}/reassign-caterer")
    public ResponseEntity<AdminActionResponse> reassignCaterer(
            @PathVariable Long userId,
            @Valid @RequestBody ReassignCatererRequest request,
            @AuthenticationPrincipal SecurityUser currentUser) {
        return ResponseEntity.ok(adminService.reassignCaterer(userId, request, currentUser));
    }
}