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
 * Helper-lər və Mapper-lər istifadə edərək təmiz kod.
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
        log.info("Təyin edilmiş istifadəçilər istənilir: email={}", dietitianEmail);

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(dietitianEmail);

        // Mapper ilə list-i response-a çevir
        return dietitianMapper.toUserSummaryList(dietitian.getUsers());
    }

    @Override
    @Transactional
    public void createMonthlyMenu(String dietitianEmail, MenuCreateRequest request) {
        log.info("Aylıq menyu yaradılır: email={}, userId={}, year={}, month={}",
                dietitianEmail, request.getUserId(), request.getYear(), request.getMonth());

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(dietitianEmail);
        UserEntity user = entityFinder.findUserById(request.getUserId());

        // Helper ilə draft batch tap və ya yarat
        MenuBatchEntity draftBatch = menuBatchHelper.getOrCreateDraftBatch(
                user, dietitian, request.getYear(), request.getMonth());

        // Dietary notes-u menu-ya yaz
        if (request.getDietaryNotes() != null) {
            draftBatch.getMenu().setDietaryNotes(request.getDietaryNotes());
        }

        // Helper ilə item-ləri əlavə və ya update et
        menuBatchHelper.addOrUpdateItems(draftBatch, request.getItems());

        menuRepository.save(draftBatch.getMenu());
        log.info("Menyu uğurla yaradıldı/yeniləndi");
    }

    @Override
    @Transactional
    public String submitMenu(Long batchId) {
        log.info("Menyu submit edilir: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Helper ilə submit et
        menuBatchHelper.submitBatch(batch);

        return "Menyu paketi istifadəçiyə təqdim edildi.";
    }

    @Override
    @Transactional
    public String updateProfile(String currentEmail, DietitianUpdateRequest request) {
        log.info("Dietitian profili yenilənir: email={}", currentEmail);

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(currentEmail);

        // Update fields
        if (request.getFirstName() != null) dietitian.setFirstName(request.getFirstName());
        if (request.getLastName() != null) dietitian.setLastName(request.getLastName());
        if (request.getSpecialization() != null) dietitian.setSpecialization(request.getSpecialization());

        // Email update + duplicate check
        if (request.getEmail() != null && !request.getEmail().equals(currentEmail)) {
            if (dietitianRepository.existsByEmail(request.getEmail())) {
                throw new ResourceAlreadyExistsException("Bu email artıq istifadədədir.");
            }
            dietitian.setEmail(request.getEmail());
        }

        // Password update
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            dietitian.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        dietitianRepository.save(dietitian);
        log.info("Profil uğurla yeniləndi");
        return "Profil məlumatlarınız uğurla yeniləndi.";
    }

    @Override
    @Transactional(readOnly = true)
    public DietitianProfileResponse getProfile(String email) {
        log.info("Dietitian profili istənilir: email={}", email);

        DietitianEntity dietitian = entityFinder.findDietitianByEmail(email);

        // Mapper ilə response-a çevir
        return dietitianMapper.toProfileResponse(dietitian);
    }

    @Override
    @Transactional(readOnly = true)
    public DietitianDashboardResponse getDashboardStats(String dietitianEmail) {
        log.info("Dashboard statistikası hesablanır: email={}", dietitianEmail);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Total patients
        long total = userRepository.countByDietitianEmail(dietitianEmail);

        // Active users
        List<UserEntity> activeUsers = userRepository.findByDietitianEmailAndStatus(
                dietitianEmail, UserStatus.ACTIVE);

        // Helper ilə approved menu sayını hesabla
        long activeMenusCount = activeUsers.stream()
                .filter(u -> menuBatchHelper.hasApprovedMenu(u.getId(), year, month))
                .count();

        // Helper ilə pending menu sayını hesabla
        long pendingMenusCount = activeUsers.stream()
                .filter(u -> menuBatchHelper.isDietitianActionRequired(u.getId(), year, month))
                .count();

        // Mapper ilə response-a çevir
        return dietitianMapper.toDashboardResponse(total, pendingMenusCount, activeMenusCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getUrgentPatients(String dietitianEmail) {
        log.info("Urgent patients istənilir: email={}", dietitianEmail);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Helper ilə filter et
        return userRepository.findByDietitianEmailAndStatus(dietitianEmail, UserStatus.ACTIVE).stream()
                .filter(user -> !menuBatchHelper.hasApprovedMenu(user.getId(), year, month))
                .map(dietitianMapper::toUrgentPatientResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientMedicalProfileResponse getPatientMedicalProfile(Long userId) {
        log.info("Patient medical profile istənilir: userId={}", userId);

        UserEntity user = entityFinder.findUserById(userId);

        HealthProfileEntity profile = user.getHealthProfile();
        if (profile == null) {
            throw new HealthProfileNotFoundException("Sağlamlıq profili tapılmadı.");
        }

        // Mapper ilə response-a çevir
        return dietitianMapper.toMedicalProfileResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMonthlyMenu(Long userId, Integer year, Integer month) {
        log.info("Aylıq menyu istənilir: userId={}, year={}, month={}", userId, year, month);

        MenuEntity menu = menuRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .orElseThrow(() -> new IdNotFoundException("Menyu tapılmadı."));

        // Helper ilə ən son batch-i tap
        MenuBatchEntity latestBatch = menuBatchHelper.getLatestBatch(menu);
        if (latestBatch == null) {
            throw new IdNotFoundException("Heç bir paket tapılmadı.");
        }

        // Mapper ilə response-a çevir
        return dietitianMapper.toMenuResponse(menu, latestBatch);
    }

    @Override
    @Transactional
    public String deleteMenuContent(Long batchId, Integer day, MealType mealType) {
        log.info("Menu content silinir: batchId={}, day={}, mealType={}", batchId, day, mealType);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Helper ilə sil
        return menuBatchHelper.deleteMenuContent(batch, day, mealType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> searchMyPatients(String dietitianEmail, String query) {
        log.info("Patient search: email={}, query={}", dietitianEmail, query);

        List<UserEntity> patients = userRepository.searchPatientsByDietitian(dietitianEmail, query);

        // Mapper ilə response-a çevir
        return dietitianMapper.toUserSummaryList(patients);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuRejectionDetailResponse getMenuRejectionReason(Long batchId) {
        log.info("Menu rejection reason istənilir: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Status yoxlaması - ResourceNotFoundException istifadə edirik (404)
        if (batch.getStatus() != MenuStatus.REJECTED) {
            throw new ResourceNotFoundException(
                    "Bu paket üçün rədd səbəbi tapılmadı. Hazırkı status: " + batch.getStatus());
        }

        UserEntity user = batch.getMenu().getUser();

        // Mapper ilə response-a çevir
        return dietitianMapper.toRejectionDetailResponse(batch, user);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalFileDetailResponse getAnalysisFileUrl(Long fileId) {
        log.info("Medical file detail istənilir: fileId={}", fileId);

        MedicalFileEntity file = medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new IdNotFoundException("Fayl tapılmadı"));

        UserEntity user = file.getHealthProfile().getUser();

        // Mapper ilə response-a çevir
        return dietitianMapper.toFileDetailResponse(file, user);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getBatchDetails(Long batchId) {
        log.info("Batch details istənilir: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);
        MenuEntity menu = batch.getMenu();

        // Mapper ilə response-a çevir
        return dietitianMapper.toMenuResponse(menu, batch);
    }

    @Override
    @Transactional
    public void updateMenu(Long batchId, MenuCreateRequest request) {
        log.info("Rejected batch yenilənir: batchId={}", batchId);

        MenuBatchEntity batch = entityFinder.findBatchById(batchId);

        // Helper ilə update et
        menuBatchHelper.updateRejectedBatch(batch, request.getItems());

        log.info("Batch uğurla yeniləndi");
    }
}