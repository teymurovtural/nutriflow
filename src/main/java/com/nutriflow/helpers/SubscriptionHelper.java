package com.nutriflow.helpers;

import com.nutriflow.entities.*;
import com.nutriflow.enums.CatererStatus;
import com.nutriflow.enums.SubscriptionStatus;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.exceptions.ResourceNotAvailableException;
import com.nutriflow.repositories.CatererRepository;
import com.nutriflow.repositories.DietitianRepository;
import com.nutriflow.repositories.SubscriptionRepository;
import com.nutriflow.repositories.UserRepository;
import com.nutriflow.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Helper class for Subscription and resource assignment.
 * Dietitian and Caterer assignment, subscription creation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionHelper {

    private final SubscriptionRepository subscriptionRepository;
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new subscription for a user.
     *
     * @param user            User entity
     * @param planName        Plan name
     * @param price           Price
     * @param durationMonths  Duration (months)
     * @return Created subscription
     */
    @Transactional
    public SubscriptionEntity createSubscription(UserEntity user, String planName, Double price, int durationMonths) {
        log.info("Creating new subscription: UserId={}, Plan={}, Price={}", user.getId(), planName, price);

        // Check if user already has a subscription
        if (user.getSubscription() != null) {
            log.warn("User already has a subscription: UserId={}", user.getId());
            throw new IllegalStateException("User already has an active subscription");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = DateUtils.addMonths(startDate, durationMonths);

        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .user(user)
                .planName(planName)
                .price(price)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription created: ID={}, EndDate={}", subscription.getId(), endDate);

        return subscription;
    }

    /**
     * Assigns the least busy dietitian to a user.
     *
     * @param user User entity
     * @return Assigned dietitian
     */
    @Transactional
    public DietitianEntity assignDietitian(UserEntity user) {
        log.info("Searching for a dietitian for user: UserId={}", user.getId());

        List<DietitianEntity> dietitians = dietitianRepository.findAll();

        DietitianEntity assignedDietitian = dietitians.stream()
                .filter(DietitianEntity::isActive)
                .min(Comparator.comparingInt(d -> d.getUsers().size()))
                .orElseThrow(() -> new ResourceNotAvailableException("No active dietitian found"));

        user.setDietitian(assignedDietitian);
        userRepository.save(user);

        log.info("Dietitian assigned: DietitianId={}, UserCount={}",
                assignedDietitian.getId(), assignedDietitian.getUsers().size());

        return assignedDietitian;
    }

    /**
     * Assigns an active caterer to a user.
     *
     * @param user User entity
     * @return Assigned caterer
     */
    @Transactional
    public CatererEntity assignCaterer(UserEntity user) {
        log.info("Assigning caterer for user: UserId={}", user.getId());

        CatererEntity caterer = catererRepository.findFirstByStatus(CatererStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotAvailableException("No active caterer found"));

        user.setCaterer(caterer);
        userRepository.save(user);

        log.info("Caterer assigned: CatererId={}, Name={}", caterer.getId(), caterer.getName());

        return caterer;
    }

    /**
     * Performs subscription and resource assignment together.
     * This method is called after a successful payment.
     *
     * @param user            User entity
     * @param planName        Plan name
     * @param price           Price
     * @param durationMonths  Duration
     */
    @Transactional
    public SubscriptionEntity finalizeSubscriptionWithResources(UserEntity user, String planName, Double price, int durationMonths) {
        log.info("========== SUBSCRIPTION FINALIZATION STARTED ==========");
        log.info("UserId: {}, Plan: {}, Price: {}", user.getId(), planName, price);

        try {
            // 1. Create subscription
            SubscriptionEntity subscription = createSubscription(user, planName, price, durationMonths);

            // 2. Assign dietitian
            DietitianEntity dietitian = assignDietitian(user);

            // 3. Assign caterer
            CatererEntity caterer = assignCaterer(user);

            // 4. Set user status to ACTIVE and link the subscription object
            user.setStatus(UserStatus.ACTIVE);
            user.setSubscription(subscription); // <-- This line is critical!

            userRepository.save(user);

            log.info("========== SUBSCRIPTION FINALIZATION COMPLETED ==========");
            log.info("SubscriptionId: {}, DietitianId: {}, CatererId: {}",
                    subscription.getId(), dietitian.getId(), caterer.getId());

            return subscription; // <-- Method now returns the created object

        } catch (Exception e) {
            log.error("Error during subscription finalization: {}", e.getMessage(), e);
            throw new RuntimeException("Subscription finalization failed", e);
        }
    }

    @Transactional
    public void cancelSubscription(UserEntity user) {
        log.info("Cancelling subscription: UserId={}", user.getId());

        SubscriptionEntity subscription = user.getSubscription();
        if (subscription != null) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
        }

        user.setStatus(UserStatus.EXPIRED);
        userRepository.save(user);

        log.info("Subscription cancelled and user transitioned to EXPIRED status");
    }

    /**
     * Renews a subscription.
     *
     * @param user              User entity
     * @param additionalMonths  Additional months to add
     */
    @Transactional
    public void renewSubscription(UserEntity user, int additionalMonths) {
        log.info("Renewing subscription: UserId={}, Additional Months={}", user.getId(), additionalMonths);

        SubscriptionEntity subscription = user.getSubscription();
        if (subscription == null) {
            throw new IllegalStateException("User has no subscription");
        }

        LocalDate newEndDate = DateUtils.addMonths(subscription.getEndDate(), additionalMonths);
        subscription.setEndDate(newEndDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(subscription);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Subscription renewed. New EndDate: {}", newEndDate);
    }

    /**
     * Checks whether a subscription has expired.
     *
     * @param subscription Subscription entity
     * @return true if expired
     */
    public boolean isSubscriptionExpired(SubscriptionEntity subscription) {
        if (subscription == null || subscription.getEndDate() == null) {
            return true;
        }
        return DateUtils.isBeforeToday(subscription.getEndDate());
    }

    /**
     * Calculates the remaining days of a subscription.
     *
     * @param subscription Subscription entity
     * @return Remaining days
     */
    public long getRemainingDays(SubscriptionEntity subscription) {
        if (subscription == null || subscription.getEndDate() == null) {
            return 0;
        }
        return DateUtils.getRemainingDays(subscription.getEndDate());
    }

    /**
     * Calculates subscription progress.
     *
     * @param subscription    Subscription entity
     * @param completedCount  Number of completed deliveries
     * @return Progress percentage
     */
    public double calculateSubscriptionProgress(SubscriptionEntity subscription, long completedCount) {
        if (subscription == null) {
            return 0.0;
        }
        return DateUtils.calculateSubscriptionProgress(
                subscription.getStartDate(),
                subscription.getEndDate(),
                completedCount
        );
    }

    /**
     * Checks whether a user has an active subscription.
     *
     * @param user User entity
     * @return true if active subscription exists
     */
    public boolean hasActiveSubscription(UserEntity user) {
        if (user == null || user.getSubscription() == null) {
            return false;
        }

        SubscriptionEntity subscription = user.getSubscription();
        return subscription.getStatus() == SubscriptionStatus.ACTIVE
                && !isSubscriptionExpired(subscription);
    }

    /**
     * Finds the least busy dietitian (without assigning).
     *
     * @return Least busy dietitian
     */
    public DietitianEntity findLeastBusyDietitian() {
        List<DietitianEntity> dietitians = dietitianRepository.findAll();

        return dietitians.stream()
                .filter(DietitianEntity::isActive)
                .min(Comparator.comparingInt(d -> d.getUsers().size()))
                .orElseThrow(() -> new ResourceNotAvailableException("No active dietitian found"));
    }

    /**
     * Calculates the number of active patients for a given dietitian.
     *
     * @param dietitianId Dietitian ID
     * @return Active patient count
     */
    public long getActivePatientsCount(Long dietitianId) {
        DietitianEntity dietitian = dietitianRepository.findById(dietitianId)
                .orElseThrow(() -> new ResourceNotAvailableException("Dietitian not found"));

        return dietitian.getUsers().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .count();
    }
}