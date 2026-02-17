package com.nutriflow.mappers;

import com.nutriflow.dto.request.AdminCreateRequest;
import com.nutriflow.dto.request.AdminProfileUpdateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Admin-ə bağlı Entity-ləri Response DTO-lara çevirmə.
 * Həmçinin loglama məlumatlarını formatlamaq üçün metodlar var.
 */
@Component
@RequiredArgsConstructor
public class AdminMapper {

    /**
     * User Entity-ni Summary Response-a çevirir (Admin paneli üçün)
     */
    public UserSummaryResponse toUserSummaryResponse(UserEntity entity) {
        if (entity == null) return null;

        return UserSummaryResponse.builder()
                .userId(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                // BirbaÅŸa HealthProfile-dan enum-u Ã§É™kirik
                .goal(entity.getHealthProfile() != null ? entity.getHealthProfile().getGoal() : null)
                .build();
    }

    /**
     * Dietitian Entity-ni Profile Response-a çevirir
     */
    public DietitianProfileResponse toDietitianResponse(DietitianEntity entity) {
        if (entity == null) return null;

        return DietitianProfileResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .specialization(entity.getSpecialization())
                .phone(entity.getPhone())
                .role(entity.getRole() != null ? entity.getRole().name() : null)
                .build();
    }

    /**
     * Caterer Entity-ni Response-a çevirir
     */
    public CatererResponse toCatererResponse(CatererEntity entity) {
        if (entity == null) return null;

        return CatererResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .status(entity.getStatus())
                .build();
    }

    /**
     * Sub-admin-i Summary Response-a çevirir
     */
    public AdminSummaryResponse toAdminSummaryResponse(AdminEntity entity) {
        if (entity == null) return null;

        return AdminSummaryResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .isActive(entity.isActive())
                .isSuperAdmin(entity.isSuperAdmin())
                .build();
    }

    /**
     * Payment Entity-ni Admin Response-a çevirir
     */
    public PaymentAdminResponse toPaymentResponse(PaymentEntity payment) {
        if (payment == null) return null;

        return PaymentAdminResponse.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .currency("AZN")
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionRef())
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .userEmail(payment.getSubscription() != null && payment.getSubscription().getUser() != null
                        ? payment.getSubscription().getUser().getEmail() : "Məlumat yoxdur")
                .build();
    }

    /**
     * Activity Log Entity-ni Response-a çevirir
     */
    public ActivityLogResponse toLogResponse(ActivityLogEntity log) {
        if (log == null) return null;

        return ActivityLogResponse.builder()
                .id(log.getId())
                .createdAt(log.getCreatedAt())
                .role(log.getRole())
                .actorId(log.getActorId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .details(log.getDetails())
                .build();
    }

    public AdminActionResponse toUserCreatedResponse(UserEntity user) {
        if (user == null) return null; //

        return AdminActionResponse.builder()
                .message("İstifadəçi və sağlamlıq profili uğurla yaradıldı")
                .targetId(user.getId()) // UserEntity-dən id-ni götürür
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .userStatus(user.getStatus()) // Entity daxilindəki ACTIVE statusunu götürür
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * Mövcud AdminEntity-ni Request-dəki məlumatlarla yeniləyir.
     */
    public void updateEntityFromRequest(AdminEntity entity, AdminCreateRequest request) {
        if (request == null || entity == null) return;

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
    }

    /**
     * AdminCreateRequest-dən yeni AdminEntity yaradır.
     */
    public AdminEntity toEntity(AdminCreateRequest request) {
        if (request == null) return null;

        return AdminEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .role(com.nutriflow.enums.Role.ADMIN)
                .isActive(true)
                .isSuperAdmin(false)
                .build();
    }

    /**
     * Əməliyyat nəticəsinə uyğun AdminActionResponse yaradır.
     */
    public AdminActionResponse toAdminActionResponse(AdminEntity saved, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(saved.getId())
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    // AdminMapper.java daxilinə əlavə et:

    /**
     * Dashboard statistikaları üçün response obyektini qurur.
     */
    public AdminDashboardResponse toDashboardResponse(
            long totalUsers,
            long totalDietitians,
            long totalCaterers,
            long activeSubscriptions,
            Double totalRevenue,
            long totalDeliveries,
            long successfulDeliveries,
            long failedDeliveries,
            long pendingMenus,
            long approvedMenus,
            long rejectedMenus,
            long newUsersThisMonth,
            Map<String, Double> chartData) {

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalDietitians(totalDietitians)
                .totalCaterers(totalCaterers)
                .activeSubscriptions(activeSubscriptions)
                .totalRevenue(totalRevenue)
                .totalDeliveries(totalDeliveries)
                .successfulDeliveries(successfulDeliveries)
                .failedDeliveries(failedDeliveries)
                .pendingMenus(pendingMenus)
                .approvedMenus(approvedMenus)
                .rejectedMenus(rejectedMenus)
                .newUsersThisMonth(newUsersThisMonth)
                .chartData(chartData)
                .build();
    }

    /**
     * Dashboard logları üçün oldData (filtr məlumatları) formatlayır.
     */
    public String formatDashboardFilterLog(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return String.format("Filtr: %s - %s", start.toLocalDate(), end.toLocalDate());
    }

    /**
     * Dashboard logları üçün newData (əsas nəticələr) formatlayır.
     */
    public String formatDashboardResultLog(Double revenue, long userCount) {
        return String.format("Gəlir: %.2f AZN, İstifadəçi: %d", revenue, userCount);
    }

    public String formatUserAssignmentOldData(UserEntity user) {
        if (user == null) return "";

        if (user.getDietitian() != null) {
            return String.format("İstifadəçi: %s %s (%s), Köhnə Dietoloq: %s %s, Status: %s",
                    user.getFirstName(), user.getLastName(), user.getStatus(),
                    user.getDietitian().getFirstName(), user.getDietitian().getLastName(),
                    user.getDietitian().isActive() ? "AKTİV" : "DEAKTİV");
        }

        if (user.getCaterer() != null) {
            return String.format("İstifadəçi: %s (%s), Köhnə Mətbəx: %s, Status: %s",
                    user.getEmail(), user.getStatus(), user.getCaterer().getName(), user.getCaterer().getStatus());
        }

        return String.format("İstifadəçi: %s (%s), Təyinat: Təyin edilməyib", user.getEmail(), user.getStatus());
    }

    public String formatDietitianAssignmentNewData(DietitianEntity dietitian) {
        return String.format("Yeni Dietoloq: %s %s, Email: %s, Status: %s",
                dietitian.getFirstName(), dietitian.getLastName(), dietitian.getEmail(),
                dietitian.isActive() ? "AKTİV" : "DEAKTİV");
    }

    public String formatCatererAssignmentNewData(CatererEntity caterer) {
        return String.format("Yeni Mətbəx: %s, Email: %s, Status: %s",
                caterer.getName(), caterer.getEmail(), caterer.getStatus());
    }


    // Ümumi AdminActionResponse yaratmaq üçün
    public AdminActionResponse toAdminActionResponse(Long targetId, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(targetId)
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }



    /**
     * AdminProfileUpdateRequest-dən gələn məlumatlarla mövcud entity-ni yeniləyir.
     * Email və Şifrə yoxlanışı Service-də aparılacaq.
     */
    public void updateAdminProfileFromRequest(AdminEntity entity, AdminProfileUpdateRequest request) {
        if (entity == null || request == null) return;

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        // Email və Password birbaşa set edilmir, çünki service-də validasiya lazımdır
    }


    // User statusu üçün xüsusi (çünki userStatus field-i var)
    public AdminActionResponse toUserStatusResponse(Long targetId, com.nutriflow.enums.UserStatus status, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(targetId)
                .userStatus(status)
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    // ========== LOGLAMA MƏLUMATLARININ FORMATLANMASI ==========
    // Bu metodlar Service-lərdə istifadə olunur (ActivityLog-da saxlanmaq üçün)

    /**
     * Dietitian məlumatlarını loqlama üçün formatlar.
     * LoggingUtil-ə dele edirik (reusability üçün).
     */
    public String formatDietitianData(DietitianEntity entity) {
        return LoggingUtils.formatDietitianData(entity);
    }

    /**
     * Caterer məlumatlarını loqlama üçün formatlar
     */
    public String formatCatererData(CatererEntity entity) {
        return LoggingUtils.formatCatererData(entity);
    }

    /**
     * User məlumatlarını loqlama üçün formatlar
     */
    public String formatUserData(UserEntity entity) {
        return LoggingUtils.formatUserData(entity);
    }

    /**
     * Admin məlumatlarını loqlama üçün formatlar
     */
    public String formatAdminData(AdminEntity entity) {
        return LoggingUtils.formatAdminData(entity);
    }

    /**
     * Payment məlumatlarını loqlama üçün formatlar
     */
    public String formatPaymentData(PaymentEntity entity) {
        return LoggingUtils.formatPaymentData(entity);
    }
}