package com.nutriflow.scheduler;

import com.nutriflow.entities.SubscriptionEntity;
import com.nutriflow.enums.SubscriptionStatus;
import com.nutriflow.repositories.SubscriptionRepository;
import com.nutriflow.services.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailNotificationService emailNotificationService;

    /**
     * ✅ YENİ: Backend başlayanda keçmiş tarixi yoxla
     * Əgər backend uzun müddət qapanıbsa, missed notification-ları göndər
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        log.info("🚀 [STARTUP] Backend başladı, keçmiş notification-ları yoxlayırıq...");

        try {
            // Bitmiş subscription-ları deaktiv et
            deactivateExpiredSubscriptions();

            // 7 gün və ya daha az qalmış subscription-lara email göndər
            checkAndNotifyUpcomingExpirations();

        } catch (Exception e) {
            log.error("❌ [STARTUP] Startup check zamanı xəta: {}", e.getMessage(), e);
        }
    }

    /**
     * Bitmiş abunəlikləri deaktiv edir
     * Hər gün saat 01:00 + Backend startup zamanı
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void deactivateExpiredSubscriptions() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        log.info("🔄 [SUBSCRIPTION] Bitmiş abunəliklərin yoxlanılması başladı");

        try {
            List<SubscriptionEntity> expiredSubscriptions = subscriptionRepository
                    .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, today);

            if (expiredSubscriptions.isEmpty()) {
                log.info("✅ [SUBSCRIPTION] Bitmiş abunəlik tapılmadı");
                return;
            }

            int deactivatedCount = 0;
            for (SubscriptionEntity subscription : expiredSubscriptions) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
                deactivatedCount++;

                log.info("⚠️ [SUBSCRIPTION] Abunəlik deaktiv edildi | User ID: {} | End Date: {}",
                        subscription.getUser().getId(), subscription.getEndDate());

                // Email göndər
                emailNotificationService.sendSubscriptionExpiredNotification(subscription);
            }

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [SUBSCRIPTION] Deaktivasiya tamamlandı | Deaktiv edilən: {} | Müddət: {}ms",
                    deactivatedCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION] Deaktivasiya zamanı xəta: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ YENİ: 7 gün və ya daha az qalmış subscription-ları yoxla
     * Startup zamanı işləyir
     */
    @Transactional(readOnly = true)
    public void checkAndNotifyUpcomingExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        log.info("📧 [SUBSCRIPTION] Yaxınlaşan bitişlər yoxlanılır (0-7 gün arası)");

        try {
            // 0-7 gün arası bitəcək bütün subscription-ları tap
            List<SubscriptionEntity> upcomingExpirations = subscriptionRepository
                    .findAll()
                    .stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                    .filter(s -> s.getEndDate().isAfter(today) &&
                            !s.getEndDate().isAfter(sevenDaysLater))
                    .toList();

            if (upcomingExpirations.isEmpty()) {
                log.info("✅ [SUBSCRIPTION] 7 gün ərzində bitəcək abunəlik yoxdur");
                return;
            }

            for (SubscriptionEntity subscription : upcomingExpirations) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, subscription.getEndDate());

                log.info("📧 [SUBSCRIPTION] Xəbərdarlıq göndərilir | User: {} | {} gün qalıb | End Date: {}",
                        subscription.getUser().getEmail(), daysLeft, subscription.getEndDate());

                emailNotificationService.sendSubscriptionExpirationWarning(subscription);
            }

            log.info("✅ [SUBSCRIPTION] Xəbərdarlıqlar göndərildi | Toplam: {}",
                    upcomingExpirations.size());

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION] Xəbərdarlıq zamanı xəta: {}", e.getMessage(), e);
        }
    }

    /**
     * Yaxınlaşan subscription bitişləri üçün xəbərdarlıq (7 gün qalmış)
     * Hər gün saat 10:00
     */
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional(readOnly = true)
    public void notifyUpcomingExpirations() {
        LocalDate sevenDaysLater = LocalDate.now().plusDays(7);

        log.info("📧 [SUBSCRIPTION] Bitəcək abunəliklər yoxlanılır (dəqiq 7 gün)");

        try {
            List<SubscriptionEntity> expiringSubscriptions = subscriptionRepository
                    .findByStatusAndEndDate(SubscriptionStatus.ACTIVE, sevenDaysLater);

            if (expiringSubscriptions.isEmpty()) {
                log.info("✅ [SUBSCRIPTION] Dəqiq 7 gün ərzində bitəcək abunəlik yoxdur");
                return;
            }

            for (SubscriptionEntity subscription : expiringSubscriptions) {
                log.info("📧 [SUBSCRIPTION] Xəbərdarlıq göndərilir | User: {} | End Date: {}",
                        subscription.getUser().getEmail(), subscription.getEndDate());

                emailNotificationService.sendSubscriptionExpirationWarning(subscription);
            }

            log.info("✅ [SUBSCRIPTION] Xəbərdarlıqlar göndərildi | Toplam: {}",
                    expiringSubscriptions.size());

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION] Xəbərdarlıq zamanı xəta: {}", e.getMessage(), e);
        }
    }

    /**
     * Subscription statistikası (hər həftə)
     */
    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional(readOnly = true)
    public void generateWeeklySubscriptionReport() {
        log.info("📊 [SUBSCRIPTION-REPORT] Həftəlik report hazırlanır...");

        try {
            long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            long expiredCount = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
            long cancelledCount = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

            log.info("📊 [SUBSCRIPTION-REPORT] Aktiv: {} | Bitmiş: {} | Ləğv edilmiş: {} | Toplam: {}",
                    activeCount, expiredCount, cancelledCount,
                    activeCount + expiredCount + cancelledCount);

            emailNotificationService.sendWeeklyReportToAdmin(activeCount, expiredCount, cancelledCount);

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION-REPORT] Report hazırlanarkən xəta: {}", e.getMessage(), e);
        }
    }
}