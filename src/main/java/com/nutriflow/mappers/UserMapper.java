package com.nutriflow.mappers;

import com.nutriflow.dto.request.RegisterRequest;
import com.nutriflow.dto.request.RegisterRequestForAdmin;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.MenuStatus;
import com.nutriflow.enums.Role;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.utils.DateUtils;
import com.nutriflow.utils.EntityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapping between User Entity and DTOs.
 * Enhanced version - response building logic in mapper.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    /**
     * Creates UserEntity from RegisterRequest.
     */
    public UserEntity toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        return UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword()) // Password will be encoded in Service
                .role(Role.USER)
                .status(UserStatus.REGISTERED)
                .isEmailVerified(false)
                .build();
    }

    public UserEntity toEntity(RegisterRequestForAdmin request) {
        if (request == null) {
            return null;
        }

        return UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true)
                .build();
    }

    public UserDashboardResponse toDashboardResponse(
            UserEntity user,
            SubscriptionEntity subscription,
            MenuStatus currentMenuStatus,
            long completedCount,
            long totalDays,
            double progress) {

        return UserDashboardResponse.builder()
                .planName(EntityUtils.getPlanName(subscription))
                .subscriptionStatus(subscription.getStatus())
                .nextRenewalDate(subscription.getEndDate())
                .menuStatus(currentMenuStatus)
                .dietitianFullName(EntityUtils.getDietitianFullName(user.getDietitian()))
                .completedDeliveries(completedCount)
                .totalDays((int) totalDays)
                .progressPercentage(progress)
                .build();
    }

    /**
     * Creates MenuResponse from Menu and Batch.
     *
     * @param menu  Menu entity
     * @param batch MenuBatch entity
     * @return MenuResponse
     */
    public MenuResponse toMenuResponse(MenuEntity menu, MenuBatchEntity batch) {
        if (menu == null || batch == null) {
            return null;
        }

        List<MenuItemResponse> itemResponses = batch.getItems().stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());

        return MenuResponse.builder()
                .menuId(menu.getId())
                .batchId(batch.getId())
                .year(menu.getYear())
                .month(menu.getMonth())
                .dietaryNotes(menu.getDietaryNotes())
                .status(batch.getStatus().name())
                .items(itemResponses)
                .build();
    }

    /**
     * Creates MenuItemResponse from MenuItemEntity.
     *
     * @param item MenuItemEntity
     * @return MenuItemResponse
     */
    public MenuItemResponse toMenuItemResponse(MenuItemEntity item) {
        if (item == null) {
            return null;
        }

        return MenuItemResponse.builder()
                .day(item.getDay())
                .mealType(item.getMealType().name())
                .description(item.getDescription())
                .calories(item.getCalories())
                .protein(item.getProtein())
                .carbs(item.getCarbs())
                .fats(item.getFats())
                .build();
    }

    /**
     * Creates Medical Profile Response from HealthProfile and User.
     *
     * @param user    User entity
     * @param profile HealthProfile entity
     * @return PatientMedicalProfileResponse
     */
    public PatientMedicalProfileResponse toMedicalProfileResponse(UserEntity user, HealthProfileEntity profile) {
        if (user == null || profile == null) {
            return null;
        }

        double bmi = EntityUtils.calculateBMI(profile);

        List<MedicalFileResponse> fileDTOs = profile.getMedicalFiles() != null
                ? profile.getMedicalFiles().stream()
                .map(this::toMedicalFileResponse)
                .collect(Collectors.toList())
                : List.of();

        return PatientMedicalProfileResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .goal(profile.getGoal().name())
                .restrictions(profile.getRestrictions())
                .notes(profile.getNotes())
                .bmi(bmi)
                .files(fileDTOs)
                .build();
    }

    /**
     * Creates MedicalFileResponse from MedicalFileEntity.
     *
     * @param file MedicalFileEntity
     * @return MedicalFileResponse
     */
    public MedicalFileResponse toMedicalFileResponse(MedicalFileEntity file) {
        if (file == null) {
            return null;
        }

        return MedicalFileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileUrl(file.getFileUrl())
                .build();
    }

    /**
     * Creates UserSummaryResponse from User Entity (for Admin panel).
     *
     * @param user User entity
     * @return UserSummaryResponse
     */
    public UserSummaryResponse toUserSummaryResponse(UserEntity user) {
        if (user == null) {
            return null;
        }

        return UserSummaryResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .goal(user.getHealthProfile() != null ? user.getHealthProfile().getGoal() : null)
                .build();
    }

    /**
     * Creates AdminActionResponse based on the operation result.
     */
    public AdminActionResponse toAdminActionResponse(UserEntity user, String message) {
        if (user == null) return null;

        return AdminActionResponse.builder()
                .message(message)
                .targetId(user.getId())
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .userStatus(user.getStatus())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }
}