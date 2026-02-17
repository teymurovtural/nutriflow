package com.nutriflow.services;

import com.nutriflow.dto.request.DietitianUpdateRequest;
import com.nutriflow.dto.request.MenuCreateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.enums.MealType;

import java.util.List;

public interface DietitianService {
    List<UserSummaryResponse> getMyAssignedUsers(String dietitianEmail);
    void createMonthlyMenu(String dietitianEmail, MenuCreateRequest request);
    String submitMenu(Long batchId);
    MenuRejectionDetailResponse getMenuRejectionReason(Long batchId);
    String updateProfile(String currentEmail, DietitianUpdateRequest request);
    DietitianProfileResponse getProfile(String email);
    DietitianDashboardResponse getDashboardStats(String dietitianEmail);
    List<UserSummaryResponse> getUrgentPatients(String dietitianEmail);
    PatientMedicalProfileResponse getPatientMedicalProfile(Long userId);
    List<UserSummaryResponse> searchMyPatients(String dietitianEmail, String query);
    MedicalFileDetailResponse getAnalysisFileUrl(Long fileId);
    MenuResponse getBatchDetails(Long batchId);
    void updateMenu(Long batchId, MenuCreateRequest request);
    MenuResponse getMonthlyMenu(Long userId, Integer year, Integer month);
    String deleteMenuContent(Long batchId, Integer day, MealType mealType);
}