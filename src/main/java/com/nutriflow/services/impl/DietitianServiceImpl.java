package com.nutriflow.services.impl;

import com.nutriflow.dto.request.DietitianUpdateRequest;
import com.nutriflow.dto.request.MenuCreateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.MealType;
import com.nutriflow.enums.MenuStatus;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.exceptions.*;
import com.nutriflow.helpers.EntityFinderHelper;
import com.nutriflow.helpers.MenuBatchHelper;
import com.nutriflow.mappers.DietitianMapper;
import com.nutriflow.repositories.*;
import com.nutriflow.services.DietitianService;
import com.nutriflow.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Dietitian Service Implementation (Refactored).
 * Clean code using Helpers and Mappers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DietitianServiceImpl implements DietitianService {

    private final UserRepository userRepository;
    private final DietitianRepository dietitianRepository;
    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;
    private final MedicalFileRepository medicalFileRepository;
    private final PasswordEncoder passwordEncoder;

    // Helpers
    private final MenuBatchHelper menuBatchHelper;
    private final EntityFinderHelper entityFinder;

    // Mappers
    private final DietitianMapper dietitianMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getMyAssignedUsers(String dietitianEmail) {
        log.info("Assigned users requested: email={}", dietitianEmail);

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(dietitianEmail);

        // Convert list to response via Mapper
        return dietitianMapper.toUserSummaryList(dietitian.getUsers());
    }

    @Override
    @Transactional
    public void createMonthlyMenu(String dietitianEmail, MenuCreateRequest request) {
        log.info("Creating monthly menu: email={}, userId={}, year={}, month={}",
                dietitianEmail, request.getUserId(), request.getYear(), request.getMonth());

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(dietitianEmail);
        UserEntity user = entityFinder.findUserById(request.getUserId());

        // Find or create draft batch via Helper
        MenuBatchEntity draftBatch = menuBatchHelper.getOrCreateDraftBatch(
                user, dietitian, request.getYear(), request.getMonth());

        // Write dietary notes to menu
        if (request.getDietaryNotes() != null) {
            draftBatch.getMenu().setDietaryNotes(request.getDietaryNotes());
        }

        // Add or update items via Helper
        menuBatchHelper.addOrUpdateItems(draftBatch, request.getItems());

        menuRepository.save(draftBatch.getMenu());
        log.info("Menu created/updated successfully");
    }

    @Override
    @Transactional
    public String submitMenu(Long batchId) {
        log.info("Submitting menu: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Submit via Helper
        menuBatchHelper.submitBatch(batch);

        return "Menu package submitted to the user.";
    }

    @Override
    @Transactional
    public String updateProfile(String currentEmail, DietitianUpdateRequest request) {
        log.info("Updating dietitian profile: email={}", currentEmail);

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(currentEmail);

        // Update fields
        if (request.getFirstName() != null) dietitian.setFirstName(request.getFirstName());
        if (request.getLastName() != null) dietitian.setLastName(request.getLastName());
        if (request.getSpecialization() != null) dietitian.setSpecialization(request.getSpecialization());

        // Email update + duplicate check
        if (request.getEmail() != null && !request.getEmail().equals(currentEmail)) {
            if (dietitianRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("This email is already in use.");
            }
            dietitian.setEmail(request.getEmail());
        }

        // Password update
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            dietitian.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        dietitianRepository.save(dietitian);
        log.info("Profile updated successfully");
        return "Your profile information has been updated successfully.";
    }

    @Override
    @Transactional(readOnly = true)
    public DietitianProfileResponse getProfile(String email) {
        log.info("Dietitian profile requested: email={}", email);

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(email);

        // Convert to response via Mapper
        return dietitianMapper.toProfileResponse(dietitian);
    }

    @Override
    @Transactional(readOnly = true)
    public DietitianDashboardResponse getDashboardStats(String dietitianEmail) {
        log.info("Calculating dashboard statistics: email={}", dietitianEmail);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Total patients
        long total = userRepository.countByDietitianEmail(dietitianEmail);

        // Active users
        List<UserEntity> activeUsers = userRepository.findByDietitianEmailAndStatus(
                dietitianEmail, UserStatus.ACTIVE);

        // Calculate approved menu count via Helper
        long activeMenusCount = activeUsers.stream()
                .filter(u -> menuBatchHelper.hasApprovedMenu(u.getId(), year, month))
                .count();

        // Calculate pending menu count via Helper
        long pendingMenusCount = activeUsers.stream()
                .filter(u -> menuBatchHelper.isDietitianActionRequired(u.getId(), year, month))
                .count();

        // Convert to response via Mapper
        return dietitianMapper.toDashboardResponse(total, pendingMenusCount, activeMenusCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUrgentPatients(String dietitianEmail) {
        log.info("Urgent patients requested: email={}", dietitianEmail);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Filter via Helper
        return userRepository.findByDietitianEmailAndStatus(dietitianEmail, UserStatus.ACTIVE).stream()
                .filter(user -> !menuBatchHelper.hasApprovedMenu(user.getId(), year, month))
                .map(dietitianMapper::toUrgentPatientResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientMedicalProfileResponse getPatientMedicalProfile(Long userId) {
        log.info("Patient medical profile requested: userId={}", userId);

        UserEntity user = entityFinder.findUserById(userId);

        HealthProfileEntity profile = user.getHealthProfile();
        if (profile == null) {
            throw new HealthProfileNotFoundException("Health profile not found.");
        }

        // Convert to response via Mapper
        return dietitianMapper.toMedicalProfileResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMonthlyMenu(Long userId, Integer year, Integer month) {
        log.info("Monthly menu requested: userId={}, year={}, month={}", userId, year, month);

        MenuEntity menu = menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseThrow(() -> new IdNotFoundException("Menu not found."));

        // Find the latest batch via Helper
        MenuBatchEntity latestBatch = menuBatchHelper.getLatestBatch(menu);
        if (latestBatch == null) {
            throw new IdNotFoundException("No package found.");
        }

        // Convert to response via Mapper
        return dietitianMapper.toMenuResponse(menu, latestBatch);
    }

    @Override
    @Transactional
    public String deleteMenuContent(Long batchId, Integer day, MealType mealType) {
        log.info("Deleting menu content: batchId={}, day={}, mealType={}", batchId, day, mealType);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Delete via Helper
        return menuBatchHelper.deleteMenuContent(batch, day, mealType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> searchMyPatients(String dietitianEmail, String query) {
        log.info("Patient search: email={}, query={}", dietitianEmail, query);

        List<UserEntity> patients = userRepository.searchPatientsByDietitian(dietitianEmail, query);

        // Convert to response via Mapper
        return dietitianMapper.toUserSummaryList(patients);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuRejectionDetailResponse getMenuRejectionReason(Long batchId) {
        log.info("Menu rejection reason requested: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Status check - using ResourceNotFoundException (404)
        if (batch.getStatus() != MenuStatus.REJECTED) {
            throw new ResourceNotFoundException(
                    "No rejection reason found for this package. Current status: " + batch.getStatus());
        }

        UserEntity user = batch.getMenu().getUser();

        // Convert to response via Mapper
        return dietitianMapper.toRejectionDetailResponse(batch, user);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalFileDetailResponse getAnalysisFileUrl(Long fileId) {
        log.info("Medical file detail requested: fileId={}", fileId);

        MedicalFileEntity file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new IdNotFoundException("File not found"));

        UserEntity user = file.getHealthProfile().getUser();

        // Convert to response via Mapper
        return dietitianMapper.toFileDetailResponse(file, user);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getBatchDetails(Long batchId) {
        log.info("Batch details requested: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);
        MenuEntity menu = batch.getMenu();

        // Convert to response via Mapper
        return dietitianMapper.toMenuResponse(menu, batch);
    }

    @Override
    @Transactional
    public void updateMenu(Long batchId, MenuCreateRequest request) {
        log.info("Updating rejected batch: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // dietaryNotes update et
        if (request.getDietaryNotes() != null) {
            batch.getMenu().setDietaryNotes(request.getDietaryNotes());
            menuRepository.save(batch.getMenu());
        }

        // Update via Helper
        menuBatchHelper.updateRejectedBatch(batch, request.getItems());

        log.info("Batch updated successfully");
    }
}