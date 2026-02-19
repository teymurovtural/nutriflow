package com.nutriflow.mappers;

import com.nutriflow.dto.request.DietitianCreateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.Role;
import com.nutriflow.utils.EntityUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapping between Dietitian Entity and DTOs.
 * Enhanced version - all response building logic in mapper.
 */
@Component
public class DietitianMapper {

    /**
     * Creates DietitianEntity from DietitianCreateRequest.
     */
    public DietitianEntity toEntity(DietitianCreateRequest request) {
        if (request == null) return null;

        return DietitianEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhoneNumber())
                .specialization(request.getSpecialization())
                .role(Role.DIETITIAN)
                .isActive(true)
                .build();
    }

    /**
     * Creates DietitianProfileResponse from DietitianEntity.
     */
    public DietitianProfileResponse toProfileResponse(DietitianEntity dietitian) {
        if (dietitian == null) return null;

        return DietitianProfileResponse.builder()
                .id(dietitian.getId())
                .firstName(dietitian.getFirstName())
                .lastName(dietitian.getLastName())
                .email(dietitian.getEmail())
                .specialization(dietitian.getSpecialization())
                .phone(dietitian.getPhone())
                .role(dietitian.getRole() != null ? dietitian.getRole().name() : null)
                .build();
    }

    /**
     * Creates UserSummaryResponse from UserEntity (for assigned patients list).
     */
    public UserSummaryResponse toUserSummaryResponse(UserEntity user) {
        if (user == null) return null;

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
     * Updates existing Entity with data from the Request.
     */
    public void updateEntityFromRequest(DietitianEntity entity, DietitianCreateRequest request) {
        if (request == null || entity == null) return;

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setSpecialization(request.getSpecialization());
        entity.setPhone(request.getPhoneNumber());
    }

    /**
     * Creates AdminActionResponse based on the operation result.
     */
    public AdminActionResponse toAdminActionResponse(DietitianEntity saved, String message) {
        return AdminActionResponse.builder()
                .message(message)
                .targetId(saved.getId())
                .operationStatus(com.nutriflow.enums.OperationStatus.SUCCESS)
                .dietitianActive(saved.isActive())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    /**
     * Converts User list to UserSummaryResponse list.
     */
    public List<UserSummaryResponse> toUserSummaryList(List<UserEntity> users) {
        if (users == null) return List.of();

        return users.stream()
                .map(this::toUserSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Creates DietitianDashboardResponse from dashboard statistics.
     */
    public DietitianDashboardResponse toDashboardResponse(
            long totalPatients,
            long pendingMenus,
            long activeMenus) {

        return DietitianDashboardResponse.builder()
                .totalPatients(totalPatients)
                .pendingMenus(pendingMenus)
                .activeMenus(activeMenus)
                .build();
    }

    /**
     * Creates PatientMedicalProfileResponse from User and HealthProfile.
     */
    public PatientMedicalProfileResponse toMedicalProfileResponse(
            UserEntity user,
            HealthProfileEntity profile) {

        if (user == null || profile == null) return null;

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
     */
    public MedicalFileResponse toMedicalFileResponse(MedicalFileEntity file) {
        if (file == null) return null;

        return MedicalFileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileUrl(file.getFileUrl())
                .build();
    }

    /**
     * Creates MenuBatch rejection detail response.
     */
    public MenuRejectionDetailResponse toRejectionDetailResponse(
            MenuBatchEntity batch,
            UserEntity user) {

        if (batch == null || user == null) return null;

        return MenuRejectionDetailResponse.builder()
                .batchId(batch.getId())
                .userId(user.getId())
                .userFullName(EntityUtils.getUserFullName(user))
                .userEmail(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .rejectionReason(batch.getRejectionReason() != null
                        ? batch.getRejectionReason()
                        : "Rejection reason not specified.")
                .build();
    }

    /**
     * Creates medical file detail response.
     */
    public MedicalFileDetailResponse toFileDetailResponse(
            MedicalFileEntity file,
            UserEntity user) {

        if (file == null || user == null) return null;

        return MedicalFileDetailResponse.builder()
                .userFullName(EntityUtils.getUserFullName(user))
                .fileName(file.getFileName())
                .fileUrl(file.getFileUrl())
                .build();
    }

    /**
     * Creates MenuResponse from MenuEntity and MenuBatchEntity.
     */
    public MenuResponse toMenuResponse(MenuEntity menu, MenuBatchEntity batch) {
        if (menu == null || batch == null) return null;

        List<MenuItemResponse> itemDTOs = batch.getItems().stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());

        return MenuResponse.builder()
                .menuId(menu.getId())
                .batchId(batch.getId())
                .year(menu.getYear())
                .month(menu.getMonth())
                .dietaryNotes(menu.getDietaryNotes())
                .status(batch.getStatus().name())
                .items(itemDTOs)
                .build();
    }

    /**
     * Creates MenuItemResponse from MenuItemEntity.
     */
    public MenuItemResponse toMenuItemResponse(MenuItemEntity item) {
        if (item == null) return null;

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
     * Creates UserSummaryResponse with special status for urgent patient.
     */
    public UserSummaryResponse toUrgentPatientResponse(UserEntity user) {
        if (user == null) return null;

        return UserSummaryResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .status("PENDING_MENU")
                .goal(user.getHealthProfile() != null ? user.getHealthProfile().getGoal() : null)
                .build();
    }
}