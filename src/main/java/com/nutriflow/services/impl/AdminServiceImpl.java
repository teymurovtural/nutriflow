package com.nutriflow.services.impl;

import com.nutriflow.constants.ActionType;
import com.nutriflow.constants.LogMessages;
import com.nutriflow.dto.request.*;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.*;
import com.nutriflow.exceptions.BusinessException;
import com.nutriflow.mappers.*;
import com.nutriflow.repositories.*;
import com.nutriflow.security.SecurityUser;
import com.nutriflow.services.ActivityLogService;
import com.nutriflow.services.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin Service Implementation - REFACTORED
 *
 * Refactoring Dəyişikliklər:
 * ✅ String formatting → LoggingUtil istifadə edir
 * ✅ Hardcoded constants → ActionType istifadə edir
 * ✅ Hardcoded messages → LogMessages istifadə edir
 * ✅ İP extraction → IpAddressUtil istifadə edir (ActivityLogService-də)
 *
 * Bütün 27 metodun refactoringləşdirilmiş versiyası
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    // ============= REPOSITORIES =============
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PaymentRepository paymentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;
    private final HealthMapper healthMapper;
    private final DeliveryRepository deliveryRepository;
    private final SubscriptionRepository subscriptionRepository;

    // ============= MAPPERS =============
    private final DietitianMapper dietitianMapper;
    private final CatererMapper catererMapper;
    private final UserMapper userMapper;
    private final AdminMapper adminMapper;

    // ============= SERVICES =============
    private final ActivityLogService activityLogService;
    private final PasswordEncoder passwordEncoder;


    // =====================================================
    // 1. CREATE METHODS
    // =====================================================

    @Override
    @Transactional
    public AdminActionResponse createDietitian(DietitianCreateRequest request, SecurityUser currentUser) {
        Optional<DietitianEntity> existing = dietitianRepository.findByEmail(request.getEmail());
        DietitianEntity dietitian;
        String actionType, oldData;

        if (existing.isPresent()) {
            dietitian = existing.get();
            oldData = adminMapper.formatDietitianData(dietitian); // Mapper-dən format
            dietitianMapper.updateEntityFromRequest(dietitian, request);
            actionType = ActionType.UPDATE_DIETITIAN;
        } else {
            dietitian = dietitianMapper.toEntity(request);
            actionType = ActionType.CREATE_DIETITIAN;
            oldData = LogMessages.NEW_RECORD;
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            dietitian.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        DietitianEntity saved = dietitianRepository.save(dietitian);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), actionType, "DIETITIAN", saved.getId(),
                oldData,
                adminMapper.formatDietitianData(saved), // Mapper-dən format
                existing.isPresent() ? LogMessages.DIETITIAN_UPDATED : LogMessages.DIETITIAN_CREATED
        );

        return dietitianMapper.toAdminActionResponse(saved, existing.isPresent() ? "Dietoloq yeniləndi" : "Yeni dietoloq yaradıldı");
    }

    @Override
    @Transactional
    public AdminActionResponse createCaterer(CatererCreateRequest request, SecurityUser currentUser) {
        Optional<CatererEntity> existing = catererRepository.findByEmail(request.getEmail());
        CatererEntity caterer;
        String actionType, oldData;

        if (existing.isPresent()) {
            caterer = existing.get();
            oldData = adminMapper.formatCatererData(caterer); // Mapper-dən format
            catererMapper.updateEntityFromRequest(caterer, request);
            actionType = ActionType.UPDATE_CATERER_ADMIN;
        } else {
            caterer = catererMapper.toEntity(request);
            actionType = ActionType.CREATE_CATERER;
            oldData = LogMessages.NEW_RECORD;
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            caterer.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        CatererEntity saved = catererRepository.save(caterer);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), actionType, "CATERER", saved.getId(),
                oldData,
                adminMapper.formatCatererData(saved), // Mapper-dən format
                existing.isPresent() ? LogMessages.CATERER_UPDATED : LogMessages.CATERER_CREATED
        );

        return catererMapper.toAdminActionResponse(saved, existing.isPresent() ? "Mətbəx yeniləndi" : "Mətbəx yaradıldı");
    }

    @Override
    @Transactional
    public AdminActionResponse createUser(RegisterRequestForAdmin request, SecurityUser currentUser) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Məlumat xətası: " + request.getEmail() + " artıq istifadə olunur!");
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getHealthData() != null) {
            user.setHealthProfile(healthMapper.toHealthProfileEntity(request.getHealthData(), user));
            user.setAddress(healthMapper.toAddressEntity(request.getHealthData(), user));
        }

        UserEntity saved = userRepository.save(user);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), ActionType.CREATE_USER, "USER", saved.getId(),
                LogMessages.NEW_RECORD,
                adminMapper.formatUserData(saved), // Mapper-dən format
                LogMessages.USER_CREATED
        );

        return userMapper.toAdminActionResponse(saved, "İstifadəçi və sağlamlıq profili uğurla yaradıldı");
    }

    @Override
    @Transactional
    public AdminActionResponse createSubAdmin(AdminCreateRequest request, SecurityUser currentUser) {
        Optional<AdminEntity> existingAdmin = adminRepository.findByEmail(request.getEmail());
        AdminEntity subAdmin;
        String actionType, oldData;

        if (existingAdmin.isPresent()) {
            subAdmin = existingAdmin.get();
            oldData = adminMapper.formatAdminData(subAdmin); // Mapper-dən format
            adminMapper.updateEntityFromRequest(subAdmin, request);
            actionType = ActionType.UPDATE_PROFILE;
        } else {
            subAdmin = adminMapper.toEntity(request);
            actionType = ActionType.CREATE_ADMIN;
            oldData = LogMessages.NEW_RECORD;
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            subAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        AdminEntity saved = adminRepository.save(subAdmin);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), actionType, "ADMIN", saved.getId(),
                oldData,
                adminMapper.formatAdminData(saved), // Mapper-dən format
                existingAdmin.isPresent() ? "Sub-Admin məlumatları yeniləndi" : LogMessages.ADMIN_CREATED
        );

        String msg = existingAdmin.isPresent() ? "Admin məlumatları yeniləndi" : "Yeni admin uğurla yaradıldı";
        return adminMapper.toAdminActionResponse(saved, msg);
    }


    // =====================================================
    // 2. ASSIGN METHODS
    // =====================================================

    @Override
    @Transactional
    public AdminActionResponse assignDietitianToUser(Long userId, Long dietitianId, SecurityUser currentUser) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("İstifadəçi tapılmadı"));

        DietitianEntity dietitian = dietitianRepository.findById(dietitianId)
                .orElseThrow(() -> new BusinessException("Dietoloq tapılmadı"));

        // Log məlumatlarını Mapper-dən alırıq
        String oldData = adminMapper.formatUserAssignmentOldData(user);
        String newData = adminMapper.formatDietitianAssignmentNewData(dietitian);

        user.setDietitian(dietitian);
        UserEntity savedUser = userRepository.save(user);

        activityLogService.logAction(
                Role.ADMIN,
                currentUser.getId(),
                ActionType.ASSIGN_DIETITIAN,
                "USER",
                savedUser.getId(),
                oldData,
                newData,
                String.format("İstifadəçiyə (%s) yeni dietoloq təyin edildi", savedUser.getEmail())
        );

        return userMapper.toAdminActionResponse(savedUser, "Dietoloq uğurla təyin edildi");
    }

    @Override
    @Transactional
    public AdminActionResponse assignCatererToUser(Long userId, Long catererId, SecurityUser currentUser) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("İstifadəçi tapılmadı"));

        CatererEntity caterer = catererRepository.findById(catererId)
                .orElseThrow(() -> new BusinessException("Mətbəx tapılmadı"));

        // Log məlumatlarını Mapper-dən alırıq
        String oldData = adminMapper.formatUserAssignmentOldData(user);
        String newData = adminMapper.formatCatererAssignmentNewData(caterer);

        user.setCaterer(caterer);
        UserEntity savedUser = userRepository.save(user);

        activityLogService.logAction(
                Role.ADMIN,
                currentUser.getId(),
                ActionType.ASSIGN_CATERER,
                "USER",
                savedUser.getId(),
                oldData,
                newData,
                String.format("İstifadəçiyə (%s) yeni mətbəx təyin edildi", savedUser.getEmail())
        );

        return userMapper.toAdminActionResponse(savedUser, "Mətbəx uğurla təyin edildi");
    }


    // =====================================================
    // 3. DASHBOARD & STATISTICS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStatistics(LocalDateTime start, LocalDateTime end, SecurityUser currentUser) {
        // 1. Vaxt intervallarının təyin edilməsi
        LocalDateTime finalStart = (start != null) ? start : LocalDateTime.now().minusMonths(6);
        LocalDateTime finalEnd = (end != null) ? end : LocalDateTime.now();

        // 2. Maliyyə məlumatlarının toplanması
        Double totalRevenue = paymentRepository.getTotalRevenueByStatus(PaymentStatus.SUCCESS);
        if (totalRevenue == null) totalRevenue = 0.0;

        List<Object[]> results = paymentRepository.getMonthlyRevenueCustomRange(finalStart, finalEnd);
        Map<String, Double> chartData = new LinkedHashMap<>();

        for (Object[] result : results) {
            String month = (result[0] != null) ? result[0].toString().trim() : "Unknown";
            Double amount = (result[1] != null) ? ((Number) result[1]).doubleValue() : 0.0;
            chartData.put(month, amount);
        }

        // 3. Loglama (AdminMapper-dən gələn formatla)
        activityLogService.logAction(
                Role.ADMIN,
                currentUser.getId(),
                ActionType.VIEW_DASHBOARD,
                "SYSTEM",
                null,
                adminMapper.formatDashboardFilterLog(finalStart, finalEnd),
                adminMapper.formatDashboardResultLog(totalRevenue, userRepository.count()),
                "Admin tərəfindən dashboard statistikalarına baxıldı"
        );

        // 4. Response-un Mapper vasitəsilə qaytarılması
        // Ayın başlanğıcı və sonu
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now();

        return adminMapper.toDashboardResponse(
                userRepository.count(),
                dietitianRepository.count(),
                catererRepository.count(),
                subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE),
                totalRevenue,
                deliveryRepository.count(),
                deliveryRepository.countByStatus(DeliveryStatus.DELIVERED),
                deliveryRepository.countByStatus(DeliveryStatus.FAILED),
                menuBatchRepository.countByStatus(MenuStatus.SUBMITTED),
                menuBatchRepository.countByStatus(MenuStatus.APPROVED),
                menuBatchRepository.countByStatus(MenuStatus.REJECTED),
                userRepository.countByCreatedAtBetween(monthStart, monthEnd),
                chartData
        );
    }


    // =====================================================
    // 4. GET ALL / SEARCH METHODS (READ-ONLY)
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(adminMapper::toUserSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DietitianProfileResponse> getAllDietitians(Pageable pageable) {
        return dietitianRepository.findAll(pageable)
                .map(adminMapper::toDietitianResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatererResponse> getAllCaterers(Pageable pageable) {
        return catererRepository.findAll(pageable)
                .map(adminMapper::toCatererResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminSummaryResponse> getAllSubAdmins(Pageable pageable) {
        return adminRepository.findAllByIsSuperAdminFalse(pageable)
                .map(adminMapper::toAdminSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryResponse> searchUsers(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getAllUsers(pageable);
        }
        return userRepository.searchUsers(query, pageable)
                .map(adminMapper::toUserSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DietitianProfileResponse> searchDietitians(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getAllDietitians(pageable);
        }
        return dietitianRepository.searchDietitians(query, pageable)
                .map(adminMapper::toDietitianResponse);
    }


    // =====================================================
    // 5. TOGGLE STATUS METHODS
    // =====================================================

    @Override
    @Transactional
    public AdminActionResponse toggleDietitianStatus(Long id, SecurityUser currentUser) {
        DietitianEntity dietitian = dietitianRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Dietoloq tapılmadı!"));

        String oldData = adminMapper.formatDietitianData(dietitian);
        boolean newStatus = !dietitian.isActive();
        dietitian.setActive(newStatus);

        DietitianEntity saved = dietitianRepository.save(dietitian);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(),
                newStatus ? ActionType.ACTIVATE_DIETITIAN : "DEACTIVATE_DIETITIAN",
                "DIETITIAN", id, oldData, adminMapper.formatDietitianData(saved),
                String.format("%s statusu %s edildi.", saved.getEmail(), newStatus ? "AKTİV" : "DEAKTİV")
        );

        return adminMapper.toAdminActionResponse(id, "Dietoloq statusu uğurla dəyişdirildi.");
    }

    @Override
    @Transactional
    public AdminActionResponse toggleUserStatus(Long id, SecurityUser currentUser) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("İstifadəçi tapılmadı!"));

        String oldData = adminMapper.formatUserData(user);
        UserStatus newStatus = (user.getStatus() == UserStatus.ACTIVE) ? UserStatus.EXPIRED : UserStatus.ACTIVE;
        user.setStatus(newStatus);

        UserEntity saved = userRepository.save(user);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), ActionType.UPDATE_USER,
                "USER", id, oldData, adminMapper.formatUserData(saved),
                String.format("%s üçün status %s -> %s dəyişdirildi.", saved.getEmail(), oldData.contains("ACTIVE") ? "ACTIVE" : "EXPIRED", newStatus)
        );

        return adminMapper.toUserStatusResponse(id, newStatus, "İstifadəçi statusu uğurla yeniləndi.");
    }

    @Override
    @Transactional
    public AdminActionResponse toggleCatererStatus(Long id, SecurityUser currentUser) {
        CatererEntity caterer = catererRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Mətbəx tapılmadı!"));

        String oldData = adminMapper.formatCatererData(caterer);
        CatererStatus newStatus = (caterer.getStatus() == CatererStatus.ACTIVE) ? CatererStatus.INACTIVE : CatererStatus.ACTIVE;
        caterer.setStatus(newStatus);

        CatererEntity saved = catererRepository.save(caterer);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(),
                newStatus == CatererStatus.ACTIVE ? ActionType.ACTIVATE_CATERER : "DEACTIVATE_CATERER",
                "CATERER", id, oldData, adminMapper.formatCatererData(saved),
                String.format("%s mətbəxinin statusu %s edildi.", saved.getName(), newStatus)
        );

        return adminMapper.toAdminActionResponse(id, "Mətbəx statusu uğurla yeniləndi.");
    }

    @Override
    @Transactional
    public AdminActionResponse toggleSubAdminStatus(Long id, SecurityUser currentUser) {
        if (id.equals(currentUser.getId())) {
            throw new BusinessException("Xəta: Öz admin hesabınızı silə bilməzsiniz!");
        }
        if (!currentUser.isSuperAdmin()) {
            throw new BusinessException("Bu əməliyyat üçün Super Admin yetkisi lazımdır!");
        }

        AdminEntity admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Admin tapılmadı!"));

        String oldData = adminMapper.formatAdminData(admin);
        boolean newStatus = !admin.isActive();
        admin.setActive(newStatus);

        AdminEntity saved = adminRepository.save(admin);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(),
                newStatus ? ActionType.ACTIVATE_ADMIN : "DEACTIVATE_ADMIN",
                "ADMIN", id, oldData, adminMapper.formatAdminData(saved),
                String.format("%s admin statusu %s edildi.", saved.getEmail(), newStatus ? "AKTİV" : "DEAKTİV")
        );

        return adminMapper.toAdminActionResponse(id, "Admin statusu uğurla dəyişdirildi.");
    }


    // =====================================================
    // 6. DELETE METHODS
    // =====================================================

    @Override
    @Transactional
    public AdminActionResponse deleteUser(Long id, SecurityUser currentUser) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("İstifadəçi tapılmadı!"));

        String oldData = adminMapper.formatUserData(user);

        userRepository.delete(user);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), ActionType.DELETE_USER,
                "USER", id, oldData, LogMessages.DELETED, LogMessages.USER_DELETED
        );

        return adminMapper.toAdminActionResponse(id, "İstifadəçi sistemdən silindi.");
    }

    @Override
    @Transactional
    public AdminActionResponse deleteDietitian(Long id, SecurityUser currentUser) {
        DietitianEntity dietitian = dietitianRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Dietoloq tapılmadı!"));

        String oldData = adminMapper.formatDietitianData(dietitian);

        // Əlaqəli user-ləri təmizləyirik
        userRepository.findAllByDietitianId(id).forEach(user -> {
            user.setDietitian(null);
            userRepository.save(user);
        });

        dietitianRepository.delete(dietitian);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), ActionType.DELETE_DIETITIAN,
                "DIETITIAN", id, oldData, LogMessages.DELETED, LogMessages.DIETITIAN_DELETED
        );

        return adminMapper.toAdminActionResponse(id, "Dietoloq uğurla silindi.");
    }

    @Override
    @Transactional
    public AdminActionResponse deleteCaterer(Long id, SecurityUser currentUser) {
        CatererEntity caterer = catererRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Mətbəx tapılmadı!"));

        String oldData = adminMapper.formatCatererData(caterer);

        // Əlaqəli user-ləri təmizləyirik
        userRepository.findAllByCatererId(id).forEach(user -> {
            user.setCaterer(null);
            userRepository.save(user);
        });

        catererRepository.delete(caterer);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), ActionType.DELETE_CATERER,
                "CATERER", id, oldData, LogMessages.DELETED, LogMessages.CATERER_DELETED
        );

        return adminMapper.toAdminActionResponse(id, "Mətbəx uğurla silindi.");
    }

    @Override
    @Transactional
    public AdminActionResponse deleteSubAdmin(Long id, SecurityUser currentUser) {
        if (id.equals(currentUser.getId())) {
            throw new BusinessException("Xəta: Öz admin hesabınızı silə bilməzsiniz!");
        }

        AdminEntity admin = adminRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Admin tapılmadı!"));

        String oldData = adminMapper.formatAdminData(admin);

        adminRepository.delete(admin);

        activityLogService.logAction(
                Role.ADMIN, currentUser.getId(), ActionType.DELETE_SUB_ADMIN,
                "ADMIN", id, oldData, LogMessages.DELETED, LogMessages.ADMIN_DELETED
        );

        return adminMapper.toAdminActionResponse(id, "Admin hesabı uğurla silindi.");
    }


    // =====================================================
    // 7. PROFILE UPDATE
    // =====================================================

    @Override
    @Transactional
    public AdminActionResponse updateAdminProfile(AdminProfileUpdateRequest request, SecurityUser currentUser) {
        AdminEntity admin = adminRepository.findById(currentUser.getId())
                .orElseThrow(() -> new BusinessException("Admin tapılmadı!"));

        // Köhnə data loq üçün formatlanır
        String oldData = adminMapper.formatAdminData(admin);

        // 1. Email dəyişikliyi yoxlanışı
        if (!admin.getEmail().equalsIgnoreCase(request.getEmail())) {
            adminRepository.findByEmail(request.getEmail()).ifPresent(a -> {
                throw new BusinessException("Bu email artıq istifadə olunur!");
            });
            admin.setEmail(request.getEmail());
        }

        // 2. Sahələrin Mapper vasitəsilə yenilənməsi
        adminMapper.updateAdminProfileFromRequest(admin, request);

        // 3. Şifrə yenilənməsi
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        AdminEntity saved = adminRepository.save(admin);

        // 4. Loqlama
        activityLogService.logAction(
                Role.ADMIN,
                currentUser.getId(),
                ActionType.UPDATE_PROFILE,
                "ADMIN",
                saved.getId(),
                oldData,
                adminMapper.formatAdminData(saved),
                LogMessages.ADMIN_PROFILE_UPDATED
        );

        // 5. Response (Daha əvvəl yaratdığımız toAdminActionResponse metodunu istifadə edirik)
        return adminMapper.toAdminActionResponse(saved.getId(), "Profil məlumatlarınız uğurla yeniləndi.");
    }


    // =====================================================
    // 8. PENDING ASSIGNMENTS & QUERY METHODS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PendingAssignmentResponse getPendingDietitianAssignments() {
        List<UserSummaryResponse> list = userRepository.findAllByStatusAndDietitianIsNull(UserStatus.ACTIVE)
                .stream()
                .map(adminMapper::toUserSummaryResponse)
                .toList();
        return PendingAssignmentResponse.builder()
                .data(list)
                .count(list.size())
                .message(list.isEmpty() ? "Hazırda dietoloq gözləyən istifadəçi yoxdur." : list.size() + " istifadəçi dietoloq gözləyir.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PendingAssignmentResponse getPendingCatererAssignments() {
        List<UserSummaryResponse> list = userRepository.findAllByStatusAndCatererIsNull(UserStatus.ACTIVE)
                .stream()
                .map(adminMapper::toUserSummaryResponse)
                .toList();
        return PendingAssignmentResponse.builder()
                .data(list)
                .count(list.size())
                .message(list.isEmpty() ? "Hazırda caterer gözləyən istifadəçi yoxdur." : list.size() + " istifadəçi caterer gözləyir.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentAdminResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAllByOrderByPaymentDateDesc(pageable)
                .map(adminMapper::toPaymentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(adminMapper::toLogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentAdminResponse getPaymentDetails(Long paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("Ödəniş tapılmadı: " + paymentId));

        return adminMapper.toPaymentResponse(payment);
    }
}