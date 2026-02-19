package com.nutriflow.services.impl;

import com.nutriflow.dto.request.MenuApproveRequest;
import com.nutriflow.dto.request.UserProfileUpdateRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.DeliveryStatus;
import com.nutriflow.enums.MenuStatus;
import com.nutriflow.exceptions.*;
import com.nutriflow.helpers.DeliveryHelper;
import com.nutriflow.helpers.EntityFinderHelper;
import com.nutriflow.helpers.MenuHelper;
import com.nutriflow.helpers.SubscriptionHelper;
import com.nutriflow.mappers.DeliveryMapper;;
import com.nutriflow.mappers.UserMapper;
import com.nutriflow.repositories.MenuBatchRepository;
import com.nutriflow.repositories.UserRepository;
import com.nutriflow.services.UserService;
import com.nutriflow.utils.DateUtils;
import com.nutriflow.utils.EntityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


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
        log.info("Dashboard summary requested: email={}", email);

        // 1. Find user
        UserEntity user = entityFinder.findUserByEmail(email);

        // 2. Check subscription
        SubscriptionEntity subscription = EntityUtils.getActiveSubscription(user);
        if (subscription == null) {
            throw new SubscriptionNotFoundException("No active subscription found.");
        }

        // 3. Determine menu status for the current month
        MenuStatus currentMenuStatus = menuHelper.getCurrentMonthMenuStatus(user);

        // 4. Calculate delivery statistics
        long totalDays = DateUtils.daysBetween(subscription.getStartDate(), subscription.getEndDate());
        long completedCount = deliveryHelper.getDeliveriesByUserAndStatus(user.getId(), DeliveryStatus.DELIVERED).size();
        double progress = subscriptionHelper.calculateSubscriptionProgress(subscription, completedCount);

        log.info("Dashboard summary prepared: UserId={}, Progress={}%", user.getId(), progress);

        // 5. Convert to response via Mapper
        return userMapper.toDashboardResponse(
                user, subscription, currentMenuStatus,
                completedCount, totalDays, progress
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getMyCurrentMenus(String email) {
        log.info("Current menus requested: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        MenuEntity menu = menuHelper.getCurrentMonthMenu(user.getId())
                .orElseThrow(() -> new IdNotFoundException("No menu found for the current month."));

        // Bütün SUBMITTED batch-ləri tap
        List<MenuBatchEntity> submittedBatches = menu.getBatches().stream()
                .filter(batch -> batch.getStatus() == MenuStatus.SUBMITTED)
                .sorted(Comparator.comparing(MenuBatchEntity::getCreatedAt))
                .toList();

        if (submittedBatches.isEmpty()) {
            throw new IdNotFoundException("No menu has been sent to you yet.");
        }

        return submittedBatches.stream()
                .map(batch -> {
                    List<MenuItemEntity> sortedItems = menuHelper.getSortedMenuItems(batch);
                    batch.setItems(sortedItems);
                    return userMapper.toMenuResponse(menu, batch);
                })
                .toList();
    }

    @Override
    @Transactional
    public void approveMenu(String email, MenuApproveRequest request) {
        log.info("Menu being approved: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Find batch
        MenuBatchEntity batch = entityFinder.findBatchById(request != null && request.getBatchId() != null ? request.getBatchId() : menuHelper.getPendingApprovalBatch(email).orElseThrow(() -> new IdNotFoundException("No package found for approval")).getId());

        // Approve via Helper
        menuHelper.approveBatch(batch);

        // Create deliveries for each day via Helper
        deliveryHelper.createDeliveriesForApprovedBatch(
                batch,
                user,
                request != null ? request.getDeliveryNotes() : null
        );

        log.info("Menu approved successfully and deliveries created: BatchId={}", batch.getId());
    }

    @Override
    @Transactional
    public void rejectMenu(Long batchId, String reason) {
        log.info("Menu being rejected: batchId={}, reason={}", batchId, reason);

        MenuBatchEntity batch = menuBatchRepository.findById(batchId)
                .orElseThrow(() -> new IdNotFoundException("Package not found"));

        // Reject via Helper
        menuHelper.rejectBatch(batch, reason);

        log.info("Menu rejected");
    }

    @Override
    @Transactional(readOnly = true)
    public PatientMedicalProfileResponse getMyMedicalProfile(String email) {
        log.info("Medical profile requested: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        HealthProfileEntity profile = user.getHealthProfile();
        if (profile == null) {
            throw new HealthProfileNotFoundException("Health profile not found");
        }

        // Convert to response via Mapper
        return userMapper.toMedicalProfileResponse(user, profile);
    }

    @Override
    @Transactional
    public void updateProfile(String email, UserProfileUpdateRequest request) {
        log.info("Profile being updated: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Update user fields
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        // Update password
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update address
        if (EntityUtils.hasAddress(user)) {
            updateAddress(user.getAddress(), request);
        }

        // Update health profile
        if (EntityUtils.hasHealthProfile(user)) {
            updateHealthProfile(user.getHealthProfile(), request);
        }

        userRepository.save(user);
        log.info("Profile updated successfully: UserId={}", user.getId());
    }

    @Override
    @Transactional
    public void cancelSubscription(String email) {
        log.info("Subscription being cancelled: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Cancel via Helper
        subscriptionHelper.cancelSubscription(user);

        log.info("Subscription cancelled");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryDetailResponse> getMyDeliveries(String email) {
        log.info("Deliveries requested: email={}", email);

        UserEntity user = entityFinder.findUserByEmail(email);

        // Get deliveries via Helper
        List<DeliveryEntity> deliveries = deliveryHelper.getDeliveriesByUserAndStatus(user.getId(), null);

        return deliveries.stream()
                .map(delivery -> {
                    // Find daily menu items via Helper
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
                    .orElseThrow(() -> new IdNotFoundException("No package found for approval"));
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