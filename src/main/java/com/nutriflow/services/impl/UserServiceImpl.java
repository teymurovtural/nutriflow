package com.nutriflow.services.impl;

import com.nutriflow.dto.request.MenuApproveRequest;
import com.nutriflow.dto.request.UserProfileUpdateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.DeliveryStatus;
import com.nutriflow.enums.MenuStatus;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.exceptions.*;
import com.nutriflow.helpers.DeliveryHelper;
import com.nutriflow.helpers.EntityFinderHelper;
import com.nutriflow.helpers.MenuHelper;
import com.nutriflow.helpers.SubscriptionHelper;
import com.nutriflow.mappers.DeliveryMapper;
import com.nutriflow.mappers.UserMapper;
import com.nutriflow.repositories.MenuBatchRepository;
import com.nutriflow.repositories.UserRepository;
import com.nutriflow.services.UserService;
import com.nutriflow.utils.DateUtils;
import com.nutriflow.utils.EntityUtils;
import com.nutriflow.utils.StatisticsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service Implementation (Refactored).
 * Helper-lər və Utility-lər istifadə edərək təmiz və oxunaqlı kod.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MenuBatchRepository menuBatchRepository;
    private final PasswordEncoder passwordEncoder;

    // Helpers
    private final DeliveryHelper deliveryHelper;
    private final MenuHelper menuHelper;
    private final SubscriptionHelper subscriptionHelper;
    private final EntityFinderHelper entityFinder;

    // Mappers
    private final DeliveryMapper deliveryMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDashboardResponse getDashboardSummary(String email) {
        log.info("Dashboard summary istənilir: email={}", email);

        // 1. User-i tap
        UserEntity user = entityFinder.findUserByEmail(email);

        // 2. Subscription-ı yoxla
        SubscriptionEntity subscription = EntityUtils.getActiveSubscription(user);
        if (subscription == null) {
            throw new SubscriptionNotFoundException("Aktiv abunəliyiniz tapılmadı.");
        }

        // 3. Cari ay üçün menu statusunu təyin et
        MenuStatus currentMenuStatus = menuHelper.getCurrentMonthMenuStatus(user);

        // 4. Çatdırılma statistikasını hesabla
        long totalDays = DateUtils.daysBetween(subscription.getStartDate(), subscription.getEndDate());
        long completedCount = deliveryHelper.getDeliveriesByUserAndStatus(user.getId(), DeliveryStatus.DELIVERED).size();
        double progress = subscriptionHelper.calculateSubscriptionProgress(subscription, completedCount);

        log.info("Dashboard summary hazırlandı: UserId={}, Progress={}%", user.getId(), progress);

        // 5. Mapper ilə response-a çevir
        return userMapper.toDashboardResponse(
                user, subscription, currentMenuStatus,
                completedCount, totalDays, progress
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MenuResponse getMyCurrentMenu(String email) {
        log.info("Cari menyu istənilir: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Helper ilə cari ayın menyusunu tap
        MenuEntity menu = menuHelper.getCurrentMonthMenu(user.getId())
                .orElseThrow(() -> new IdNotFoundException("Cari ay üçün menyu tapılmadı."));

        // Helper ilə aktiv batch-i tap
        MenuBatchEntity activeBatch = menuHelper.getActiveBatch(menu)
                .orElseThrow(() -> new IdNotFoundException("Hələ ki sizə göndərilmiş bir menyu yoxdur."));

        // Helper ilə sorted items al
        List<MenuItemEntity> sortedItems = menuHelper.getSortedMenuItems(activeBatch);

        // Sorted items-i batch-ə set et (mapper istifadə edəcək)
        activeBatch.setItems(sortedItems);

        log.info("Menyu göndərilir: MenuId={}, BatchId={}, Items={}",
                menu.getId(), activeBatch.getId(), sortedItems.size());

        // Mapper ilə response-a çevir
        return userMapper.toMenuResponse(menu, activeBatch);
    }

    @Override
    @Transactional
    public void approveMenu(String email, MenuApproveRequest request) {
        log.info("Menyu təsdiq edilir: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Batch-i tap
        MenuBatchEntity batch = entityFinder.findBatchById(request != null && request.getBatchId() != null ? request.getBatchId() : menuHelper.getPendingApprovalBatch(email).orElseThrow(() -> new IdNotFoundException("Təsdiqləcək paket tapılmadı")).getId());

        // Helper ilə approve et
        menuHelper.approveBatch(batch);

        // Helper ilə hər gün üçün delivery yarat
        deliveryHelper.createDeliveriesForApprovedBatch(
                batch,
                user,
                request != null ? request.getDeliveryNotes() : null
        );


        log.info("Menyu uğurla təsdiqləndi və deliverylər yaradıldı: BatchId={}", batch.getId());
    }

    @Override
    @Transactional
    public void rejectMenu(Long batchId, String reason) {
        log.info("Menyu reject edilir: batchId={}, reason={}", batchId, reason);

        MenuBatchEntity batch = menuBatchRepository.findById(batchId)
                .orElseThrow(() -> new IdNotFoundException("Paket tapılmadı"));

        // Helper ilə reject et
        menuHelper.rejectBatch(batch, reason);

        log.info("Menyu reject edildi");
    }

    @Override
    @Transactional(readOnly = true)
    public PatientMedicalProfileResponse getMyMedicalProfile(String email) {
        log.info("Medical profile istənilir: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        HealthProfileEntity profile = user.getHealthProfile();
        if (profile == null) {
            throw new HealthProfileNotFoundException("Health profile tapılmadı");
        }

        // Mapper ilə response-a çevir
        return userMapper.toMedicalProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public void updateProfile(String email, UserProfileUpdateRequest request) {
        log.info("Profil yenilənir: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // User məlumatlarını yenilə
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        // Şifrəni yenilə
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Address yenilə
        if (EntityUtils.hasAddress(user)) {
            updateAddress(user.getAddress(), request);
        }

        // Health Profile yenilə
        if (EntityUtils.hasHealthProfile(user)) {
            updateHealthProfile(user.getHealthProfile(), request);
        }

        userRepository.save(user);
        log.info("Profil uğurla yeniləndi: UserId={}", user.getId());
    }

    @Override
    @Transactional
    public void cancelSubscription(String email) {
        log.info("Subscription cancel edilir: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Helper ilə cancel et
        subscriptionHelper.cancelSubscription(user);

        log.info("Subscription cancel edildi");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDetailResponse> getMyDeliveries(String email) {
        log.info("Delivery-lər istənilir: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Helper ilə delivery-ləri al
        List<DeliveryEntity> deliveries = deliveryHelper.getDeliveriesByUserAndStatus(user.getId(), null);

        return deliveries.stream()
                .map(delivery -> {
                    // Həmin günün yemək itemlərini helper ilə tap
                    List<MenuItemEntity> dailyItems = deliveryHelper.getMenuItemsForDay(
                            delivery.getBatch(),
                            delivery.getDate().getDayOfMonth()
                    );

                    return deliveryMapper.toDetailResponse(delivery, dailyItems);
                })
                .collect(Collectors.toList());
    }

    // ==================== Private Helper Methods ====================

    private MenuBatchEntity findBatchForApproval(String email, MenuApproveRequest request) {
        if (request == null || request.getBatchId() == null) {
            return menuHelper.getPendingApprovalBatch(email)
                    .orElseThrow(() -> new IdNotFoundException("Təsdiqləcək paket tapılmadı"));
        }

        return entityFinder.findBatchById(request.getBatchId());
    }

    private void updateAddress(AddressEntity address, UserProfileUpdateRequest request) {
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getAddressDetails() != null) address.setAddressDetails(request.getAddressDetails());
        if (request.getDeliveryNotes() != null) address.setDeliveryNotes(request.getDeliveryNotes());
    }

    private void updateHealthProfile(HealthProfileEntity profile, UserProfileUpdateRequest request) {
        if (request.getWeight() != null) profile.setWeight(request.getWeight());
        if (request.getHeight() != null) profile.setHeight(request.getHeight());
        if (request.getGoal() != null) profile.setGoal(request.getGoal());
        if (request.getRestrictions() != null) profile.setRestrictions(request.getRestrictions());
        if (request.getNotes() != null) profile.setNotes(request.getNotes());
    }
}