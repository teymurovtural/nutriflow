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
     * ✅ NEW: Check past dates when backend starts
     * If the backend was down for a long time, send missed notifications
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onStartup() {
        log.info("🚀 [STARTUP] Backend started, checking past notifications...");

        try {
            // Deactivate expired subscriptions
            deactivateExpiredSubscriptions();

            // Send emails to subscriptions with 7 or fewer days remaining
            checkAndNotifyUpcomingExpirations();

        } catch (Exception e) {
            log.error("❌ [STARTUP] Error during startup check: {}", e.getMessage(), e);
        }
    }

    /**
     * Deactivates expired subscriptions
     * Every day at 01:00 + on backend startup
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void deactivateExpiredSubscriptions() {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        log.info("🔄 [SUBSCRIPTION] Checking for expired subscriptions started");

        try {
            List<SubscriptionEntity> expiredSubscriptions = subscriptionRepository
                    .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, today);

            if (expiredSubscriptions.isEmpty()) {
                log.info("✅ [SUBSCRIPTION] No expired subscriptions found");
                return;
            }

            int deactivatedCount = 0;
            for (SubscriptionEntity subscription : expiredSubscriptions) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
                deactivatedCount++;

                log.info("⚠️ [SUBSCRIPTION] Subscription deactivated | User ID: {} | End Date: {}",
                        subscription.getUser().getId(), subscription.getEndDate());

                // Send email
                emailNotificationService.sendSubscriptionExpiredNotification(subscription);
            }

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [SUBSCRIPTION] Deactivation completed | Deactivated: {} | Duration: {}ms",
                    deactivatedCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION] Error during deactivation: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ NEW: Check subscriptions with 7 or fewer days remaining
     * Runs on startup
     */
    @Transactional(readOnly = true)
    public void checkAndNotifyUpcomingExpirations() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        log.info("📧 [SUBSCRIPTION] Checking upcoming expirations (0-7 days)");

        try {
            // Find all subscriptions expiring within 0-7 days
            List<SubscriptionEntity> upcomingExpirations = subscriptionRepository
                    .findAll()
                    .stream()
                    .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                    .filter(s -> s.getEndDate().isAfter(today) &&
                            !s.getEndDate().isAfter(sevenDaysLater))
                    .toList();

            if (upcomingExpirations.isEmpty()) {
                log.info("✅ [SUBSCRIPTION] No subscriptions expiring within 7 days");
                return;
            }

            for (SubscriptionEntity subscription : upcomingExpirations) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, subscription.getEndDate());

                log.info("📧 [SUBSCRIPTION] Sending warning | User: {} | {} days remaining | End Date: {}",
                        subscription.getUser().getEmail(), daysLeft, subscription.getEndDate());

                emailNotificationService.sendSubscriptionExpirationWarning(subscription);
            }

            log.info("✅ [SUBSCRIPTION] Warnings sent | Total: {}",
                    upcomingExpirations.size());

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION] Error during warning: {}", e.getMessage(), e);
        }
    }

    /**
     * Warning for upcoming subscription expirations (exactly 7 days remaining)
     * Every day at 10:00
     */
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional(readOnly = true)
    public void notifyUpcomingExpirations() {
        LocalDate sevenDaysLater = LocalDate.now().plusDays(7);

        log.info("📧 [SUBSCRIPTION] Checking expiring subscriptions (exactly 7 days)");

        try {
            List<SubscriptionEntity> expiringSubscriptions = subscriptionRepository
                    .findByStatusAndEndDate(SubscriptionStatus.ACTIVE, sevenDaysLater);

            if (expiringSubscriptions.isEmpty()) {
                log.info("✅ [SUBSCRIPTION] No subscriptions expiring in exactly 7 days");
                return;
            }

            for (SubscriptionEntity subscription : expiringSubscriptions) {
                log.info("📧 [SUBSCRIPTION] Sending warning | User: {} | End Date: {}",
                        subscription.getUser().getEmail(), subscription.getEndDate());

                emailNotificationService.sendSubscriptionExpirationWarning(subscription);
            }

            log.info("✅ [SUBSCRIPTION] Warnings sent | Total: {}",
                    expiringSubscriptions.size());

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION] Error during warning: {}", e.getMessage(), e);
        }
    }

    /**
     * Subscription statistics (every week)
     */
    @Scheduled(cron = "0 0 9 * * MON")
    @Transactional(readOnly = true)
    public void generateWeeklySubscriptionReport() {
        log.info("📊 [SUBSCRIPTION-REPORT] Preparing weekly report...");

        try {
            long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            long expiredCount = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
            long cancelledCount = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

            log.info("📊 [SUBSCRIPTION-REPORT] Active: {} | Expired: {} | Cancelled: {} | Total: {}",
                    activeCount, expiredCount, cancelledCount,
                    activeCount + expiredCount + cancelledCount);

            emailNotificationService.sendWeeklyReportToAdmin(activeCount, expiredCount, cancelledCount);

        } catch (Exception e) {
            log.error("❌ [SUBSCRIPTION-REPORT] Error while preparing report: {}", e.getMessage(), e);
        }
    }
}