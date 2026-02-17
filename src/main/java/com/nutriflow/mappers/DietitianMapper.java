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
 * Dietitian Entity və DTO-lar arasında mapping.
 * Enhanced version - bütün response building logic mapper-də.
 */
@Component
public class DietitianMapper {

    /**
     * DietitianCreateRequest-dən DietitianEntity yaradır.
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
     * DietitianEntity-dən DietitianProfileResponse yaradır.
     */
    public DietitianProfileResponse toProfileResponse(DietitianEntity dietitian) {
        if (dietitian == null) return null;

        return DietitianProfileResponse.builder()
                .firstName(dietitian.getFirstName())
                .lastName(dietitian.getLastName())
                .email(dietitian.getEmail())
                .specialization(dietitian.getSpecialization())
                .phone(dietitian.getPhone())
                .role(dietitian.getRole() != null ? dietitian.getRole().name() : null)
                .build();
    }

    /**
     * UserEntity-dən UserSummaryResponse yaradır (assigned patients list üçün).
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
     * Mövcud Entity-ni Request-dəki məlumatlarla yeniləyir.
     */
    public void updateEntityFromRequest(DietitianEntity entity, DietitianCreateRequest request) {
        if (request == null || entity == null) return;

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setSpecialization(request.getSpecialization());
        entity.setPhone(request.getPhoneNumber());
    }

    /**
     * Əməliyyat nəticəsinə uyğun AdminActionResponse yaradır.
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
     * User list-dən UserSummaryResponse list-ə çevirir.
     */
    public List<UserSummaryResponse> toUserSummaryList(List<UserEntity> users) {
        if (users == null) return List.of();

        return users.stream()
                .map(this::toUserSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Dashboard statistikasından DietitianDashboardResponse yaradır.
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
     * User və HealthProfile-dan PatientMedicalProfileResponse yaradır.
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
     * MedicalFileEntity-dən MedicalFileResponse yaradır.
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
     * MenuBatch rejection detail response yaradır.
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
                        : "İmtina səbəbi qeyd olunmayıb.")
                .build();
    }

    /**
     * Medical file detail response yaradır.
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
     * MenuEntity və MenuBatchEntity-dən MenuResponse yaradır.
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
     * MenuItemEntity-dən MenuItemResponse yaradır.
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
     * Urgent patient üçün special status ilə UserSummaryResponse yaradır.
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