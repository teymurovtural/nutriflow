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
 * Subscription və resource assignment üçün helper sinif.
 * Dietitian və Caterer təyinatı, subscription yaratma.
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
     * User üçün yeni subscription yaradır.
     *
     * @param user      User entity
     * @param planName  Plan adı
     * @param price     Qiymət
     * @param durationMonths Müddət (ay)
     * @return Yaradılmış subscription
     */
    @Transactional
    public SubscriptionEntity createSubscription(UserEntity user, String planName, Double price, int durationMonths) {
        log.info("Yeni subscription yaradılır: UserId={}, Plan={}, Price={}", user.getId(), planName, price);

        // Əvvəlcə user-in subscription-ı var mı yoxla
        if (user.getSubscription() != null) {
            log.warn("User-in artıq subscription-ı var: UserId={}", user.getId());
            throw new IllegalStateException("User-in artıq aktiv subscription-ı var");
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
        log.info("Subscription yaradıldı: ID={}, EndDate={}", subscription.getId(), endDate);

        return subscription;
    }

    /**
     * User üçün ən az yüklü dietitian təyin edir.
     *
     * @param user User entity
     * @return Təyin edilmiş dietitian
     */
    @Transactional
    public DietitianEntity assignDietitian(UserEntity user) {
        log.info("User üçün dietitian axtarılır: UserId={}", user.getId());

        List<DietitianEntity> dietitians = dietitianRepository.findAll();

        DietitianEntity assignedDietitian = dietitians.stream()
                .filter(DietitianEntity::isActive)
                .min(Comparator.comparingInt(d -> d.getUsers().size()))
                .orElseThrow(() -> new ResourceNotAvailableException("Aktiv dietitian tapılmadı"));

        user.setDietitian(assignedDietitian);
        userRepository.save(user);

        log.info("Dietitian təyin edildi: DietitianId={}, UserCount={}",
                assignedDietitian.getId(), assignedDietitian.getUsers().size());

        return assignedDietitian;
    }

    /**
     * User üçün aktiv caterer təyin edir.
     *
     * @param user User entity
     * @return Təyin edilmiş caterer
     */
    @Transactional
    public CatererEntity assignCaterer(UserEntity user) {
        log.info("User üçün caterer təyin edilir: UserId={}", user.getId());

        CatererEntity caterer = catererRepository.findFirstByStatus(CatererStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotAvailableException("Aktiv caterer tapılmadı"));

        user.setCaterer(caterer);
        userRepository.save(user);

        log.info("Caterer təyin edildi: CatererId={}, Name={}", caterer.getId(), caterer.getName());

        return caterer;
    }

    /**
     * Subscription və resource assignment-i birlikdə yerinə yetirir.
     * Payment uğurlu olduqdan sonra bu metod çağrılır.
     *
     * @param user User entity
     * @param planName Plan adı
     * @param price Qiymət
     * @param durationMonths Müddət
     */
    @Transactional
    public SubscriptionEntity finalizeSubscriptionWithResources(UserEntity user, String planName, Double price, int durationMonths) {
        log.info("========== SUBSCRIPTION FİNALİZATİON BAŞLADI ==========");
        log.info("UserId: {}, Plan: {}, Price: {}", user.getId(), planName, price);

        try {
            // 1. Subscription yarat
            SubscriptionEntity subscription = createSubscription(user, planName, price, durationMonths);

            // 2. Dietitian təyin et
            DietitianEntity dietitian = assignDietitian(user);

            // 3. Caterer təyin et
            CatererEntity caterer = assignCaterer(user);

            // 4. User statusunu ACTIVE et və abunəliyi obyektə bağla
            user.setStatus(UserStatus.ACTIVE);
            user.setSubscription(subscription); // <-- Bu sətir çox vacibdir!

            userRepository.save(user);

            log.info("========== SUBSCRIPTION FİNALİZATİON TAMAMLANDI ==========");
            log.info("SubscriptionId: {}, DietitianId: {}, CatererId: {}",
                    subscription.getId(), dietitian.getId(), caterer.getId());

            return subscription; // <-- Artıq metod yaranmış obyekti geri qaytarır

        } catch (Exception e) {
            log.error("Subscription finalization zamanı xəta: {}", e.getMessage(), e);
            throw new RuntimeException("Subscription finalization uğursuz oldu", e);
        }
    }


    @Transactional
    public void cancelSubscription(UserEntity user) {
        log.info("Subscription cancel edilir: UserId={}", user.getId());

        SubscriptionEntity subscription = user.getSubscription();
        if (subscription != null) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(subscription);
        }

        user.setStatus(UserStatus.EXPIRED);
        userRepository.save(user);

        log.info("Subscription cancel edildi və user EXPIRED statusuna keçdi");
    }

    /**
     * Subscription-ı yeniləyir (renew).
     *
     * @param user User entity
     * @param additionalMonths Əlavə ay sayı
     */
    @Transactional
    public void renewSubscription(UserEntity user, int additionalMonths) {
        log.info("Subscription yenilənir: UserId={}, Additional Months={}", user.getId(), additionalMonths);

        SubscriptionEntity subscription = user.getSubscription();
        if (subscription == null) {
            throw new IllegalStateException("User-in subscription-ı yoxdur");
        }

        LocalDate newEndDate = DateUtils.addMonths(subscription.getEndDate(), additionalMonths);
        subscription.setEndDate(newEndDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(subscription);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Subscription yeniləndi. Yeni EndDate: {}", newEndDate);
    }

    /**
     * Subscription-ın müddətinin bitib-bitmədiyini yoxlayır.
     *
     * @param subscription Subscription entity
     * @return true əgər müddət bitibsə
     */
    public boolean isSubscriptionExpired(SubscriptionEntity subscription) {
        if (subscription == null || subscription.getEndDate() == null) {
            return true;
        }
        return DateUtils.isBeforeToday(subscription.getEndDate());
    }

    /**
     * Subscription-ın qalan günlərini hesablayır.
     *
     * @param subscription Subscription entity
     * @return Qalan günlər
     */
    public long getRemainingDays(SubscriptionEntity subscription) {
        if (subscription == null || subscription.getEndDate() == null) {
            return 0;
        }
        return DateUtils.getRemainingDays(subscription.getEndDate());
    }

    /**
     * Subscription progress-i hesablayır.
     *
     * @param subscription   Subscription entity
     * @param completedCount Tamamlanmış çatdırılma sayı
     * @return Progress faizi
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
     * User-in subscription-ının aktiv olub-olmadığını yoxlayır.
     *
     * @param user User entity
     * @return true əgər aktiv subscription varsa
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
     * Ən az yüklü dietitian-ı tapır (təyin etmədən).
     *
     * @return Ən az yüklü dietitian
     */
    public DietitianEntity findLeastBusyDietitian() {
        List<DietitianEntity> dietitians = dietitianRepository.findAll();

        return dietitians.stream()
                .filter(DietitianEntity::isActive)
                .min(Comparator.comparingInt(d -> d.getUsers().size()))
                .orElseThrow(() -> new ResourceNotAvailableException("Aktiv dietitian tapılmadı"));
    }

    /**
     * Müəyyən dietitian-ın neçə aktiv müştərisi olduğunu hesablayır.
     *
     * @param dietitianId Dietitian ID
     * @return Aktiv müştəri sayı
     */
    public long getActivePatientsCount(Long dietitianId) {
        DietitianEntity dietitian = dietitianRepository.findById(dietitianId)
                .orElseThrow(() -> new ResourceNotAvailableException("Dietitian tapılmadı"));

        return dietitian.getUsers().stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .count();
    }
}