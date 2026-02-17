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
    // Yaradılma və Təyinat metodları
    AdminActionResponse createDietitian(DietitianCreateRequest request, SecurityUser currentUser);
    AdminActionResponse createCaterer(CatererCreateRequest request, SecurityUser currentUser);
    AdminActionResponse createUser(RegisterRequestForAdmin request, SecurityUser currentUser);
    AdminActionResponse createSubAdmin(AdminCreateRequest request, SecurityUser currentUser);
    AdminActionResponse assignDietitianToUser(Long userId, Long dietitianId, SecurityUser currentUser);
    AdminActionResponse assignCatererToUser(Long userId, Long catererId, SecurityUser currentUser);
    AdminDashboardResponse getDashboardStatistics(LocalDateTime start, LocalDateTime end, SecurityUser currentUser);

    // Siyahılama (DTO ilə yeniləndi)
    Page<UserSummaryResponse> getAllUsers(Pageable pageable);
    Page<DietitianProfileResponse> getAllDietitians(Pageable pageable);
    Page<CatererResponse> getAllCaterers(Pageable pageable);
    Page<AdminSummaryResponse> getAllSubAdmins(Pageable pageable); // AdminAuthResponse deyil, Summary istifadə edirik
    Page<UserSummaryResponse> searchUsers(String query, Pageable pageable);
    Page<DietitianProfileResponse> searchDietitians(String query, Pageable pageable);

    // Silmə və Status əməliyyatları
    AdminActionResponse toggleDietitianStatus(Long id, SecurityUser currentUser);
    AdminActionResponse toggleUserStatus(Long id, SecurityUser currentUser);
    AdminActionResponse toggleCatererStatus(Long id, SecurityUser currentUser);
    AdminActionResponse toggleSubAdminStatus(Long id, SecurityUser currentUser);
    AdminActionResponse deleteUser(Long id, SecurityUser currentUser);
    AdminActionResponse deleteDietitian(Long id, SecurityUser currentUser);
    AdminActionResponse deleteCaterer(Long id, SecurityUser currentUser);
    AdminActionResponse deleteSubAdmin(Long id, SecurityUser currentUser);

    // Ödənişlər və Loqlar (Hələlik Entity olaraq qala bilər, amma DTO tövsiyə olunur)
    Page<PaymentAdminResponse> getAllPayments(Pageable pageable);
    Page<ActivityLogResponse> getAllActivityLogs(Pageable pageable);
    PaymentAdminResponse getPaymentDetails(Long paymentId);

    // Digər
    AdminActionResponse updateAdminProfile(AdminProfileUpdateRequest request, SecurityUser currentUser);
    PendingAssignmentResponse getPendingDietitianAssignments();
    PendingAssignmentResponse getPendingCatererAssignments();
}
