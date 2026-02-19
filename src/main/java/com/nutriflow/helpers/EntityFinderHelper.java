package com.nutriflow.helpers;

import com.nutriflow.entities.*;
import com.nutriflow.exceptions.IdNotFoundException;
import com.nutriflow.exceptions.UserNotFoundException;
import com.nutriflow.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Entity Finder Helper - Reusable finder methods for all services.
 * Finds entities from repository and throws exceptions if not found.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EntityFinderHelper {

    private final UserRepository userRepository;
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;
    private final AdminRepository adminRepository;
    private final MenuRepository menuRepository;
    private final MenuBatchRepository menuBatchRepository;
    private final DeliveryRepository deliveryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final MedicalFileRepository medicalFileRepository;

    /**
     * Finds a User by ID.
     *
     * @param userId User ID
     * @return UserEntity
     * @throws UserNotFoundException if not found
     */
    public UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    }

    /**
     * Finds a User by email.
     *
     * @param email User email
     * @return UserEntity
     * @throws UserNotFoundException if not found
     */
    public UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    /**
     * Finds a Dietitian by email.
     *
     * @param email Dietitian email
     * @return DietitianEntity
     * @throws UserNotFoundException if not found
     */
    public DietitianEntity findDietitianByEmail(String email) {
        return dietitianRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Dietitian not found: " + email));
    }

    /**
     * Finds a Dietitian by ID.
     *
     * @param dietitianId Dietitian ID
     * @return DietitianEntity
     * @throws UserNotFoundException if not found
     */
    public DietitianEntity findDietitianById(Long dietitianId) {
        return dietitianRepository.findById(dietitianId)
                .orElseThrow(() -> new UserNotFoundException("Dietitian not found: " + dietitianId));
    }

    /**
     * Finds a Caterer by ID.
     *
     * @param catererId Caterer ID
     * @return CatererEntity
     * @throws IdNotFoundException if not found
     */
    public CatererEntity findCatererById(Long catererId) {
        return catererRepository.findById(catererId)
                .orElseThrow(() -> new IdNotFoundException("Caterer not found: " + catererId));
    }

    /**
     * Finds a Caterer by email.
     *
     * @param email Caterer email
     * @return CatererEntity
     * @throws UserNotFoundException if not found
     */
    public CatererEntity findCatererByEmail(String email) {
        return catererRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Caterer not found: " + email));
    }

    /**
     * Finds an Admin by ID.
     *
     * @param adminId Admin ID
     * @return AdminEntity
     * @throws UserNotFoundException if not found
     */
    public AdminEntity findAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin not found: " + adminId));
    }

    /**
     * Finds an Admin by email.
     *
     * @param email Admin email
     * @return AdminEntity
     * @throws UserNotFoundException if not found
     */
    public AdminEntity findAdminByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Admin not found: " + email));
    }

    /**
     * Finds a Menu by ID.
     *
     * @param menuId Menu ID
     * @return MenuEntity
     * @throws IdNotFoundException if not found
     */
    public MenuEntity findMenuById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IdNotFoundException("Menu not found: " + menuId));
    }

    /**
     * Finds a MenuBatch by ID.
     *
     * @param batchId Batch ID
     * @return MenuBatchEntity
     * @throws IdNotFoundException if not found
     */
    public MenuBatchEntity findBatchById(Long batchId) {
        return menuBatchRepository.findById(batchId)
                .orElseThrow(() -> new IdNotFoundException("Batch not found: " + batchId));
    }

    /**
     * Finds a Delivery by ID.
     *
     * @param deliveryId Delivery ID
     * @return DeliveryEntity
     * @throws IdNotFoundException if not found
     */
    public DeliveryEntity findDeliveryById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IdNotFoundException("Delivery not found: " + deliveryId));
    }

    /**
     * Finds a Subscription by ID.
     *
     * @param subscriptionId Subscription ID
     * @return SubscriptionEntity
     * @throws IdNotFoundException if not found
     */
    public SubscriptionEntity findSubscriptionById(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IdNotFoundException("Subscription not found: " + subscriptionId));
    }

    /**
     * Finds a HealthProfile by ID.
     *
     * @param healthProfileId HealthProfile ID
     * @return HealthProfileEntity
     * @throws IdNotFoundException if not found
     */
    public HealthProfileEntity findHealthProfileById(Long healthProfileId) {
        return healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> new IdNotFoundException("Health profile not found: " + healthProfileId));
    }

    /**
     * Finds a user's HealthProfile.
     *
     * @param user UserEntity
     * @return HealthProfileEntity
     * @throws IdNotFoundException if not found
     */
    public HealthProfileEntity findHealthProfileByUser(UserEntity user) {
        if (user.getHealthProfile() == null) {
            throw new IdNotFoundException("User has no health profile: " + user.getId());
        }
        return user.getHealthProfile();
    }

    /**
     * Finds a user's active Subscription.
     *
     * @param user UserEntity
     * @return SubscriptionEntity
     * @throws IdNotFoundException if not found
     */
    public SubscriptionEntity findActiveSubscription(UserEntity user) {
        if (user.getSubscription() == null) {
            throw new IdNotFoundException("User has no active subscription: " + user.getId());
        }
        return user.getSubscription();
    }

    public MedicalFileEntity findMedicalFileById(Long fileId) {
        return medicalFileRepository.findById(fileId)
                .orElseThrow(() -> new IdNotFoundException("Medical file not found: " + fileId));
    }

    public MedicalFileEntity findMedicalFileByIdAndHealthProfileId(Long fileId, Long healthProfileId) {
        return medicalFileRepository.findByIdAndHealthProfileId(fileId, healthProfileId)
                .orElseThrow(() -> new IdNotFoundException(
                        "Medical file not found or you do not have permission: " + fileId));
    }

}