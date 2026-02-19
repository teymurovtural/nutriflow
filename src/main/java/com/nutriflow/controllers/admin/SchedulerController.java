package com.nutriflow.controllers.admin;

import com.nutriflow.entities.SubscriptionEntity;
import com.nutriflow.enums.SubscriptionStatus;
import com.nutriflow.repositories.SubscriptionRepository;
import com.nutriflow.repositories.UserRepository;
import com.nutriflow.scheduler.DatabaseCleanupScheduler;
import com.nutriflow.scheduler.RedisCleanupScheduler;
import com.nutriflow.scheduler.SubscriptionScheduler;
import com.nutriflow.services.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/scheduler-test")
@RequiredArgsConstructor
@Slf4j
public class SchedulerController {

    private final DatabaseCleanupScheduler databaseCleanupScheduler;
    private final SubscriptionScheduler subscriptionScheduler;
    private final RedisCleanupScheduler redisCleanupScheduler;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailNotificationService emailNotificationService;
    private final UserRepository userRepository;

    // ==================== STATUS ====================

    @GetMapping("/status")
    public ResponseEntity<String> getSchedulerStatus() {
        return ResponseEntity.ok(
                "✅ Scheduler Service Active\n\n" +
                        "📋 Available schedulers:\n" +
                        "1. Database Cleanup (1st of every month at 03:00)\n" +
                        "2. Subscription Deactivation (Every day at 01:00)\n" +
                        "3. Redis OTP Cleanup (Every hour)\n" +
                        "4. Redis Token Cleanup (Every day at 04:00)\n" +
                        "5. Redis Stats (Every 6 hours)\n" +
                        "6. Subscription Expiration Warning (Every day at 10:00)\n" +
                        "7. Weekly Subscription Report (Monday at 09:00)"
        );
    }

    // ==================== DATABASE CLEANUP ====================

    @PostMapping("/database-cleanup")
    public ResponseEntity<String> testDatabaseCleanup() {
        try {
            log.info("📋 Manual database cleanup test started");
            databaseCleanupScheduler.cleanupOldDeliveries();
            return ResponseEntity.ok("✅ Database cleanup executed successfully");
        } catch (Exception e) {
            log.error("❌ Database cleanup error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== SUBSCRIPTION DEACTIVATION ====================

    @PostMapping("/subscription-deactivate")
    public ResponseEntity<String> testSubscriptionDeactivate() {
        try {
            log.info("📋 Manual subscription deactivation test started");
            subscriptionScheduler.deactivateExpiredSubscriptions();
            return ResponseEntity.ok("✅ Subscription deactivation executed successfully");
        } catch (Exception e) {
            log.error("❌ Subscription deactivation error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-subscription-warning")
    public ResponseEntity<String> testSubscriptionWarning() {
        try {
            log.info("📋 Manual subscription warning test started");
            subscriptionScheduler.notifyUpcomingExpirations();
            return ResponseEntity.ok("✅ Subscription warning executed successfully");
        } catch (Exception e) {
            log.error("❌ Subscription warning error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== REDIS ====================

    @PostMapping("/redis-stats")
    public ResponseEntity<String> testRedisStats() {
        try {
            log.info("📋 Manual Redis stats test started");
            redisCleanupScheduler.logRedisStatistics();
            return ResponseEntity.ok("✅ Redis stats executed successfully");
        } catch (Exception e) {
            log.error("❌ Redis stats error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== EMAIL TESTS ====================

    @PostMapping("/test-email")
    @Transactional
    public ResponseEntity<String> testEmail() {
        try {
            var testSubscription = subscriptionRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No subscription found in database"));

            var userEmail = testSubscription.getUser().getEmail();
            emailNotificationService.sendSubscriptionExpirationWarning(testSubscription);

            return ResponseEntity.ok("✅ Test email sent: " + userEmail);
        } catch (Exception e) {
            log.error("❌ Test email error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-admin-report")
    public ResponseEntity<String> testAdminReport() {
        try {
            long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            long expiredCount = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
            long cancelledCount = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

            emailNotificationService.sendWeeklyReportToAdmin(activeCount, expiredCount, cancelledCount);

            return ResponseEntity.ok("✅ Admin report email sent");
        } catch (Exception e) {
            log.error("❌ Admin report email error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== TEST DATA CREATION ====================

    @PostMapping("/create-test-subscription")
    @Transactional
    public ResponseEntity<String> createTestSubscription() {
        try {
            var testUser = userRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No user found in database"));

            // ✅ FIX: Do not delete old, UPDATE instead
            var existingSub = subscriptionRepository.findByUserId(testUser.getId());

            if (existingSub.isPresent()) {
                var sub = existingSub.get();
                sub.setPlanName("Premium Test");
                sub.setPrice(15.0);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setStartDate(LocalDate.now());
                sub.setEndDate(LocalDate.now().plusDays(30));
                var saved = subscriptionRepository.save(sub);

                return ResponseEntity.ok("✅ Test subscription UPDATED: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate());
            } else {
                var subscription = SubscriptionEntity.builder()
                        .user(testUser)
                        .planName("Premium Test")
                        .price(15.0)
                        .status(SubscriptionStatus.ACTIVE)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(30))
                        .build();

                var saved = subscriptionRepository.save(subscription);

                return ResponseEntity.ok("✅ Test subscription CREATED: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate());
            }
        } catch (Exception e) {
            log.error("❌ Test subscription error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * ✅ FIX: Subscription expiring in 7 days - UPDATE, do not delete
     */
    @PostMapping("/create-expiring-subscription")
    @Transactional
    public ResponseEntity<String> createExpiringSubscription() {
        try {
            var testUser = userRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No user found in database"));

            // ✅ Find old and UPDATE
            var existingSub = subscriptionRepository.findByUserId(testUser.getId());

            if (existingSub.isPresent()) {
                var sub = existingSub.get();
                sub.setPlanName("Premium Test - Expiring");
                sub.setPrice(15.0);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setStartDate(LocalDate.now());
                sub.setEndDate(LocalDate.now().plusDays(7)); // ✅ 7 days
                var saved = subscriptionRepository.save(sub);

                return ResponseEntity.ok("✅ Subscription expiring in 7 days UPDATED: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate());
            } else {
                var subscription = SubscriptionEntity.builder()
                        .user(testUser)
                        .planName("Premium Test - Expiring")
                        .price(15.0)
                        .status(SubscriptionStatus.ACTIVE)
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusDays(7))
                        .build();

                var saved = subscriptionRepository.save(subscription);

                return ResponseEntity.ok("✅ Subscription expiring in 7 days CREATED: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate());
            }
        } catch (Exception e) {
            log.error("❌ Expiring subscription error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    /**
     * ✅ FIX: Expired subscription - UPDATE, do not delete
     */
    @PostMapping("/create-expired-subscription")
    @Transactional
    public ResponseEntity<String> createExpiredSubscription() {
        try {
            var testUser = userRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No user found in database"));

            // ✅ Find old and UPDATE
            var existingSub = subscriptionRepository.findByUserId(testUser.getId());

            if (existingSub.isPresent()) {
                var sub = existingSub.get();
                sub.setPlanName("Premium Test - Expired");
                sub.setPrice(15.0);
                sub.setStatus(SubscriptionStatus.ACTIVE); // Still active
                sub.setStartDate(LocalDate.now().minusDays(8));
                sub.setEndDate(LocalDate.now().minusDays(1)); // Expired yesterday
                var saved = subscriptionRepository.save(sub);

                return ResponseEntity.ok("✅ Expired subscription UPDATED: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate() + " (yesterday)");
            } else {
                var subscription = SubscriptionEntity.builder()
                        .user(testUser)
                        .planName("Premium Test - Expired")
                        .price(15.0)
                        .status(SubscriptionStatus.ACTIVE)
                        .startDate(LocalDate.now().minusDays(8))
                        .endDate(LocalDate.now().minusDays(1))
                        .build();

                var saved = subscriptionRepository.save(subscription);

                return ResponseEntity.ok("✅ Expired subscription CREATED: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate() + " (yesterday)");
            }
        } catch (Exception e) {
            log.error("❌ Expired subscription error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    // ==================== HELPER ENDPOINTS ====================

    @GetMapping("/subscription-count")
    public ResponseEntity<String> getSubscriptionCount() {
        try {
            long total = subscriptionRepository.count();
            long active = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            long expired = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
            long cancelled = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

            return ResponseEntity.ok(String.format(
                    "📊 Subscription Statistics:\n" +
                            "Total: %d\n" +
                            "✅ Active: %d\n" +
                            "❌ Expired: %d\n" +
                            "🚫 Cancelled: %d",
                    total, active, expired, cancelled
            ));
        } catch (Exception e) {
            log.error("❌ Statistics error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/cleanup-test-data")
    @Transactional
    public ResponseEntity<String> cleanupTestData() {
        try {
            long deletedCount = 0;

            var testSubs = subscriptionRepository.findAll().stream()
                    .filter(sub -> sub.getPlanName() != null && sub.getPlanName().contains("Test"))
                    .toList();

            for (var sub : testSubs) {
                subscriptionRepository.delete(sub);
                deletedCount++;
                log.info("🗑️ Test subscription deleted: ID={}", sub.getId());
            }

            return ResponseEntity.ok("✅ " + deletedCount + " test subscriptions deleted");
        } catch (Exception e) {
            log.error("❌ Cleanup error", e);
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }
    }
}