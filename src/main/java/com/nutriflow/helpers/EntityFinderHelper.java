package com.nutriflow.helpers;

import com.nutriflow.entities.*;
import com.nutriflow.exceptions.IdNotFoundException;
import com.nutriflow.exceptions.UserNotFoundException;
import com.nutriflow.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Entity Finder Helper - Bütün service-lər üçün təkrarlanan finder metodları.
 * Repository-dən entity tapır və exception throw edir.
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

    /**
     * User-i ID-yə görə tapır.
     *
     * @param userId User ID
     * @return UserEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public UserEntity findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı: " + userId));
    }

    /**
     * User-i email-ə görə tapır.
     *
     * @param email User email
     * @return UserEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı: " + email));
    }

    /**
     * Dietitian-ı email-ə görə tapır.
     *
     * @param email Dietitian email
     * @return DietitianEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public DietitianEntity findDietitianByEmail(String email) {
        return dietitianRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Dietoloq tapılmadı: " + email));
    }

    /**
     * Dietitian-ı ID-yə görə tapır.
     *
     * @param dietitianId Dietitian ID
     * @return DietitianEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public DietitianEntity findDietitianById(Long dietitianId) {
        return dietitianRepository.findById(dietitianId)
                .orElseThrow(() -> new UserNotFoundException("Dietoloq tapılmadı: " + dietitianId));
    }

    /**
     * Caterer-i ID-yə görə tapır.
     *
     * @param catererId Caterer ID
     * @return CatererEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public CatererEntity findCatererById(Long catererId) {
        return catererRepository.findById(catererId)
                .orElseThrow(() -> new IdNotFoundException("Caterer tapılmadı: " + catererId));
    }

    /**
     * Caterer-i email-ə görə tapır.
     *
     * @param email Caterer email
     * @return CatererEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public CatererEntity findCatererByEmail(String email) {
        return catererRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Caterer tapılmadı: " + email));
    }

    /**
     * Admin-i ID-yə görə tapır.
     *
     * @param adminId Admin ID
     * @return AdminEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public AdminEntity findAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new UserNotFoundException("Admin tapılmadı: " + adminId));
    }

    /**
     * Admin-i email-ə görə tapır.
     *
     * @param email Admin email
     * @return AdminEntity
     * @throws UserNotFoundException Tapılmazsa
     */
    public AdminEntity findAdminByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Admin tapılmadı: " + email));
    }

    /**
     * Menu-nu ID-yə görə tapır.
     *
     * @param menuId Menu ID
     * @return MenuEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public MenuEntity findMenuById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new IdNotFoundException("Menyu tapılmadı: " + menuId));
    }

    /**
     * MenuBatch-i ID-yə görə tapır.
     *
     * @param batchId Batch ID
     * @return MenuBatchEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public MenuBatchEntity findBatchById(Long batchId) {
        return menuBatchRepository.findById(batchId)
                .orElseThrow(() -> new IdNotFoundException("Paket tapılmadı: " + batchId));
    }

    /**
     * Delivery-ni ID-yə görə tapır.
     *
     * @param deliveryId Delivery ID
     * @return DeliveryEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public DeliveryEntity findDeliveryById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IdNotFoundException("Çatdırılma tapılmadı: " + deliveryId));
    }

    /**
     * Subscription-ı ID-yə görə tapır.
     *
     * @param subscriptionId Subscription ID
     * @return SubscriptionEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public SubscriptionEntity findSubscriptionById(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IdNotFoundException("Abunəlik tapılmadı: " + subscriptionId));
    }

    /**
     * HealthProfile-i ID-yə görə tapır.
     *
     * @param healthProfileId HealthProfile ID
     * @return HealthProfileEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public HealthProfileEntity findHealthProfileById(Long healthProfileId) {
        return healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> new IdNotFoundException("Sağlamlıq profili tapılmadı: " + healthProfileId));
    }

    /**
     * User-in HealthProfile-ini tapır.
     *
     * @param user UserEntity
     * @return HealthProfileEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public HealthProfileEntity findHealthProfileByUser(UserEntity user) {
        if (user.getHealthProfile() == null) {
            throw new IdNotFoundException("İstifadəçinin sağlamlıq profili yoxdur: " + user.getId());
        }
        return user.getHealthProfile();
    }

    /**
     * User-in aktiv Subscription-ını tapır.
     *
     * @param user UserEntity
     * @return SubscriptionEntity
     * @throws IdNotFoundException Tapılmazsa
     */
    public SubscriptionEntity findActiveSubscription(UserEntity user) {
        if (user.getSubscription() == null) {
            throw new IdNotFoundException("İstifadəçinin aktiv abunəliyi yoxdur: " + user.getId());
        }
        return user.getSubscription();
    }
}