package com.nutriflow.services;

import com.nutriflow.dto.request.*;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.ActivityLogEntity;
import com.nutriflow.entities.PaymentEntity;
import com.nutriflow.security.SecurityUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminService {
    // Creation and Assignment methods
    AdminActionResponse createDietitian(DietitianCreateRequest request, SecurityUser currentUser);
    AdminActionResponse createCaterer(CatererCreateRequest request, SecurityUser currentUser);
    AdminActionResponse createUser(RegisterRequestForAdmin request, SecurityUser currentUser);
    AdminActionResponse createSubAdmin(AdminCreateRequest request, SecurityUser currentUser);
    AdminActionResponse assignDietitianToUser(Long userId, Long dietitianId, SecurityUser currentUser);
    AdminActionResponse assignCatererToUser(Long userId, Long catererId, SecurityUser currentUser);
    AdminDashboardResponse getDashboardStatistics(LocalDateTime start, LocalDateTime end, SecurityUser currentUser);

    // Listing (updated with DTO)
    Page<UserSummaryResponse> getAllUsers(Pageable pageable);
    Page<DietitianProfileResponse> getAllDietitians(Pageable pageable);
    Page<CatererResponse> getAllCaterers(Pageable pageable);
    Page<AdminSummaryResponse> getAllSubAdmins(Pageable pageable); // Using Summary instead of AdminAuthResponse
    Page<UserSummaryResponse> searchUsers(String query, Pageable pageable);
    Page<DietitianProfileResponse> searchDietitians(String query, Pageable pageable);

    // Delete and Status operations
    AdminActionResponse toggleDietitianStatus(Long id, SecurityUser currentUser);
    AdminActionResponse toggleUserStatus(Long id, SecurityUser currentUser);
    AdminActionResponse toggleCatererStatus(Long id, SecurityUser currentUser);
    AdminActionResponse toggleSubAdminStatus(Long id, SecurityUser currentUser);
    AdminActionResponse deleteUser(Long id, SecurityUser currentUser);
    AdminActionResponse deleteDietitian(Long id, SecurityUser currentUser);
    AdminActionResponse deleteCaterer(Long id, SecurityUser currentUser);
    AdminActionResponse deleteSubAdmin(Long id, SecurityUser currentUser);

    // Payments and Logs (can remain as Entity for now, but DTO is recommended)
    Page<PaymentAdminResponse> getAllPayments(Pageable pageable);
    Page<ActivityLogResponse> getAllActivityLogs(Pageable pageable);
    PaymentAdminResponse getPaymentDetails(Long paymentId);

    // Other
    AdminActionResponse updateAdminProfile(AdminProfileUpdateRequest request, SecurityUser currentUser);
    PendingAssignmentResponse getPendingDietitianAssignments();
    PendingAssignmentResponse getPendingCatererAssignments();

    UserSummaryResponse getUserById(Long userId);
    SubscriptionInfoResponse getUserSubscriptionInfo(Long userId);
    DietitianProfileResponse getDietitianById(Long id);
    CatererResponse getCatererById(Long id);
    Page<MenuBatchAdminResponse> getAllMenuBatches(Pageable pageable);
    MenuBatchAdminResponse getMenuBatchById(Long batchId);
}