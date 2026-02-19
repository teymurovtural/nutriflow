package com.nutriflow.mappers;

import com.nutriflow.dto.request.AdminCreateRequest;
import com.nutriflow.dto.request.AdminProfileUpdateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.DeliveryStatus;
import com.nutriflow.utils.LoggingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * Converts Admin-related Entities to Response DTOs.
 * Also contains methods for formatting logging information.
 */
@Component
@RequiredArgsConstructor
public class AdminMapper {

    /**
     * Converts User Entity to Summary Response (for Admin panel)
     */
    public UserSummaryResponse toUserSummaryResponse(UserEntity user) {
        if (user == null) return null;

        HealthProfileEntity hp = user.getHealthProfile();

        return UserSummaryResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .goal(hp != null ? hp.getGoal() : null)
                .height(hp != null ? hp.getHeight() : null)
                .weight(hp != null ? hp.getWeight() : null)
                .restrictions(hp != null ? hp.getRestrictions() : null)
                .notes(hp != null ? hp.getNotes() : null)
                .dietitianFullName(user.getDietitian() != null
                        ? user.getDietitian().getFirstName() + " " + user.getDietitian().getLastName()
                        : null)
                .catererFullName(user.getCaterer() != null
                        ? user.getCaterer().getName()
                        : null)
                .build();
    }

    /**
     * Converts Dietitian Entity to Profile Response
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
                .totalPatients(entity.getUsers() != null ? entity.getUsers().size() : 0)
                .build();
    }

    /**
     * Converts Caterer Entity to Response
     */
    public CatererResponse toCatererResponse(CatererEntity entity) {
        if (entity == null) return null;

        LocalDate today = LocalDate.now();

        long total = entity.getDeliveries() != null ? entity.getDeliveries().size() : 0;
        long delivered = entity.getDeliveries() != null ? entity.getDeliveries().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED).count() : 0;
        long failed = entity.getDeliveries() != null ? entity.getDeliveries().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.FAILED).count() : 0;
        long inProgress = total - delivered - failed;

        // Bugünkü statistika
        long todayTotal = entity.getDeliveries() != null ? entity.getDeliveries().stream()
                .filter(d -> d.getCreatedAt().toLocalDate().isEqual(today)).count() : 0;
        long todayDelivered = entity.getDeliveries() != null ? entity.getDeliveries().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.DELIVERED
                        && d.getCreatedAt().toLocalDate().isEqual(today)).count() : 0;
        long todayFailed = entity.getDeliveries() != null ? entity.getDeliveries().stream()
                .filter(d -> d.getStatus() == DeliveryStatus.FAILED
                        && d.getCreatedAt().toLocalDate().isEqual(today)).count() : 0;
        long todayInProgress = todayTotal - todayDelivered - todayFailed;

        return CatererResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .status(entity.getStatus())
                .totalDeliveries(total)
                .deliveredCount(delivered)
                .failedCount(failed)
                .inProgressCount(inProgress)
                .todayDeliveries(todayTotal)
                .todayDelivered(todayDelivered)
                .todayFailed(todayFailed)
                .todayInProgress(todayInProgress)
                .build();
    }

    /**
     * Converts Sub-admin to Summary Response
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
     * Converts Payment Entity to Admin Response
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
                        ? payment.getSubscription().getUser().getEmail() : "No data available")
                .build();
    }

    /**
     * Converts Activity Log Entity to Response
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
        if (user == null) return null;

        return AdminActionResponse.builder()
                .message("User and health profile created successfully")
                .targetId(user.getId())
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .userStatus(user.getStatus())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    public MenuBatchAdminResponse toMenuBatchAdminResponse(MenuBatchEntity batch) {
        if (batch == null) return null;

        MenuEntity menu = batch.getMenu();
        UserEntity user = menu.getUser();
        DietitianEntity dietitian = menu.getDietitian();

        return MenuBatchAdminResponse.builder()
                .batchId(batch.getId())
                .menuId(menu.getId())
                .userFullName(user.getFirstName() + " " + user.getLastName())
                .dietitianFullName(dietitian.getFirstName() + " " + dietitian.getLastName())
                .catererFullName(user.getCaterer() != null
                        ? user.getCaterer().getName()
                        : null)
                .status(batch.getStatus())
                .rejectionReason(batch.getRejectionReason())
                .year(menu.getYear())
                .month(menu.getMonth())
                .totalItems(batch.getItems() != null ? batch.getItems().size() : 0)
                .createdAt(batch.getCreatedAt())
                .build();
    }

    /**
     * Updates existing AdminEntity with data from the Request.
     */
    public void updateEntityFromRequest(AdminEntity entity, AdminCreateRequest request) {
        if (request == null || entity == null) return;

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
    }

    /**
     * Creates a new AdminEntity from AdminCreateRequest.
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
     * Creates AdminActionResponse based on the operation result.
     */
    public AdminActionResponse toAdminActionResponse(AdminEntity saved, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(saved.getId())
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * Builds response object for dashboard statistics.
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
     * Formats oldData (filter information) for dashboard logs.
     */
    public String formatDashboardFilterLog(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return String.format("Filter: %s - %s", start.toLocalDate(), end.toLocalDate());
    }

    /**
     * Formats newData (main results) for dashboard logs.
     */
    public String formatDashboardResultLog(Double revenue, long userCount) {
        return String.format("Revenue: %.2f AZN, Users: %d", revenue, userCount);
    }

    public String formatUserAssignmentOldData(UserEntity user) {
        if (user == null) return "";

        if (user.getDietitian() != null) {
            return String.format("User: %s %s (%s), Previous Dietitian: %s %s, Status: %s",
                    user.getFirstName(), user.getLastName(), user.getStatus(),
                    user.getDietitian().getFirstName(), user.getDietitian().getLastName(),
                    user.getDietitian().isActive() ? "ACTIVE" : "INACTIVE");
        }

        if (user.getCaterer() != null) {
            return String.format("User: %s (%s), Previous Caterer: %s, Status: %s",
                    user.getEmail(), user.getStatus(), user.getCaterer().getName(), user.getCaterer().getStatus());
        }

        return String.format("User: %s (%s), Assignment: Not assigned", user.getEmail(), user.getStatus());
    }

    public String formatDietitianAssignmentNewData(DietitianEntity dietitian) {
        return String.format("New Dietitian: %s %s, Email: %s, Status: %s",
                dietitian.getFirstName(), dietitian.getLastName(), dietitian.getEmail(),
                dietitian.isActive() ? "ACTIVE" : "INACTIVE");
    }

    public String formatCatererAssignmentNewData(CatererEntity caterer) {
        return String.format("New Caterer: %s, Email: %s, Status: %s",
                caterer.getName(), caterer.getEmail(), caterer.getStatus());
    }

    // General method for creating AdminActionResponse
    public AdminActionResponse toAdminActionResponse(Long targetId, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(targetId)
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * Updates existing entity with data from AdminProfileUpdateRequest.
     * Email and password validation will be handled in the Service layer.
     */
    public void updateAdminProfileFromRequest(AdminEntity entity, AdminProfileUpdateRequest request) {
        if (entity == null || request == null) return;

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        // Email and Password are not set directly here, as validation is required in the service
    }

    // Special response for user status (because userStatus field exists)
    public AdminActionResponse toUserStatusResponse(Long targetId, com.nutriflow.enums.UserStatus status, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(targetId)
                .userStatus(status)
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    // ========== LOGGING DATA FORMATTING ==========
    // These methods are used in Services (to be stored in ActivityLog)

    /**
     * Formats Dietitian data for logging.
     * Delegates to LoggingUtils (for reusability).
     */
    public String formatDietitianData(DietitianEntity entity) {
        return LoggingUtils.formatDietitianData(entity);
    }

    /**
     * Formats Caterer data for logging.
     */
    public String formatCatererData(CatererEntity entity) {
        return LoggingUtils.formatCatererData(entity);
    }

    /**
     * Formats User data for logging.
     */
    public String formatUserData(UserEntity entity) {
        return LoggingUtils.formatUserData(entity);
    }

    /**
     * Formats Admin data for logging.
     */
    public String formatAdminData(AdminEntity entity) {
        return LoggingUtils.formatAdminData(entity);
    }

    /**
     * Formats Payment data for logging.
     */
    public String formatPaymentData(PaymentEntity entity) {
        return LoggingUtils.formatPaymentData(entity);
    }
}