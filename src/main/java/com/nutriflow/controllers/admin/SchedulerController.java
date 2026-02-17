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
                "✅ Scheduler Service Aktiv\n\n" +
                        "📋 Mövcud scheduler-lər:\n" +
                        "1. Database Cleanup (Hər ayın 1-də saat 03:00)\n" +
                        "2. Subscription Deactivation (Hər gün saat 01:00)\n" +
                        "3. Redis OTP Cleanup (Hər saat başı)\n" +
                        "4. Redis Token Cleanup (Hər gün saat 04:00)\n" +
                        "5. Redis Stats (Hər 6 saatda bir)\n" +
                        "6. Subscription Expiration Warning (Hər gün saat 10:00)\n" +
                        "7. Weekly Subscription Report (Bazar ertəsi saat 09:00)"
        );
    }

    // ==================== DATABASE CLEANUP ====================

    @PostMapping("/database-cleanup")
    public ResponseEntity<String> testDatabaseCleanup() {
        try {
            log.info("📋 Manual database cleanup test başladı");
            databaseCleanupScheduler.cleanupOldDeliveries();
            return ResponseEntity.ok("✅ Database cleanup uğurla icra edildi");
        } catch (Exception e) {
            log.error("❌ Database cleanup xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    // ==================== SUBSCRIPTION DEACTIVATION ====================

    @PostMapping("/subscription-deactivate")
    public ResponseEntity<String> testSubscriptionDeactivate() {
        try {
            log.info("📋 Manual subscription deactivation test başladı");
            subscriptionScheduler.deactivateExpiredSubscriptions();
            return ResponseEntity.ok("✅ Subscription deactivation uğurla icra edildi");
        } catch (Exception e) {
            log.error("❌ Subscription deactivation xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    @PostMapping("/test-subscription-warning")
    public ResponseEntity<String> testSubscriptionWarning() {
        try {
            log.info("📋 Manual subscription warning test başladı");
            subscriptionScheduler.notifyUpcomingExpirations();
            return ResponseEntity.ok("✅ Subscription warning uğurla icra edildi");
        } catch (Exception e) {
            log.error("❌ Subscription warning xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    // ==================== REDIS ====================

    @PostMapping("/redis-stats")
    public ResponseEntity<String> testRedisStats() {
        try {
            log.info("📋 Manual Redis stats test başladı");
            redisCleanupScheduler.logRedisStatistics();
            return ResponseEntity.ok("✅ Redis stats uğurla icra edildi");
        } catch (Exception e) {
            log.error("❌ Redis stats xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    // ==================== EMAIL TESTS ====================

    @PostMapping("/test-email")
    @Transactional
    public ResponseEntity<String> testEmail() {
        try {
            var testSubscription = subscriptionRepository.findById(1L)
                    .orElseThrow(() -> new RuntimeException("ID=1 subscription tapılmadı"));

            var userEmail = testSubscription.getUser().getEmail();
            emailNotificationService.sendSubscriptionExpirationWarning(testSubscription);

            return ResponseEntity.ok("✅ Test email göndərildi: " + userEmail);
        } catch (Exception e) {
            log.error("❌ Test email xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    @PostMapping("/test-admin-report")
    public ResponseEntity<String> testAdminReport() {
        try {
            long activeCount = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
            long expiredCount = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
            long cancelledCount = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

            emailNotificationService.sendWeeklyReportToAdmin(activeCount, expiredCount, cancelledCount);

            return ResponseEntity.ok("✅ Admin report email göndərildi");
        } catch (Exception e) {
            log.error("❌ Admin report email xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    // ==================== TEST DATA CREATION ====================

    @PostMapping("/create-test-subscription")
    @Transactional
    public ResponseEntity<String> createTestSubscription() {
        try {
            var testUser = userRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Database-də user yoxdur"));

            // ✅ FIX: Köhnəni sil deyil, UPDATE et
            var existingSub = subscriptionRepository.findByUserId(testUser.getId());

            if (existingSub.isPresent()) {
                var sub = existingSub.get();
                sub.setPlanName("Premium Test");
                sub.setPrice(15.0);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setStartDate(LocalDate.now());
                sub.setEndDate(LocalDate.now().plusDays(30));
                var saved = subscriptionRepository.save(sub);

                return ResponseEntity.ok("✅ Test subscription UPDATE edildi: ID=" + saved.getId() +
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

                return ResponseEntity.ok("✅ Test subscription YARADILDI: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate());
            }
        } catch (Exception e) {
            log.error("❌ Test subscription xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    /**
     * ✅ FIX: 7 gün sonra bitəcək subscription - UPDATE et, silmə
     */
    @PostMapping("/create-expiring-subscription")
    @Transactional
    public ResponseEntity<String> createExpiringSubscription() {
        try {
            var testUser = userRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Database-də user yoxdur"));

            // ✅ Köhnəni tap və UPDATE et
            var existingSub = subscriptionRepository.findByUserId(testUser.getId());

            if (existingSub.isPresent()) {
                var sub = existingSub.get();
                sub.setPlanName("Premium Test - Expiring");
                sub.setPrice(15.0);
                sub.setStatus(SubscriptionStatus.ACTIVE);
                sub.setStartDate(LocalDate.now());
                sub.setEndDate(LocalDate.now().plusDays(7)); // ✅ 7 gün
                var saved = subscriptionRepository.save(sub);

                return ResponseEntity.ok("✅ 7 gün sonra bitəcək subscription UPDATE edildi: ID=" + saved.getId() +
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

                return ResponseEntity.ok("✅ 7 gün sonra bitəcək subscription YARADILDI: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate());
            }
        } catch (Exception e) {
            log.error("❌ Expiring subscription xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }

    /**
     * ✅ FIX: Bitmiş subscription - UPDATE et, silmə
     */
    @PostMapping("/create-expired-subscription")
    @Transactional
    public ResponseEntity<String> createExpiredSubscription() {
        try {
            var testUser = userRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Database-də user yoxdur"));

            // ✅ Köhnəni tap və UPDATE et
            var existingSub = subscriptionRepository.findByUserId(testUser.getId());

            if (existingSub.isPresent()) {
                var sub = existingSub.get();
                sub.setPlanName("Premium Test - Expired");
                sub.setPrice(15.0);
                sub.setStatus(SubscriptionStatus.ACTIVE); // Hələ aktiv
                sub.setStartDate(LocalDate.now().minusDays(8));
                sub.setEndDate(LocalDate.now().minusDays(1)); // Dünən bitib
                var saved = subscriptionRepository.save(sub);

                return ResponseEntity.ok("✅ Bitmiş subscription UPDATE edildi: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate() + " (dünən)");
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

                return ResponseEntity.ok("✅ Bitmiş subscription YARADILDI: ID=" + saved.getId() +
                        " | User: " + testUser.getEmail() +
                        " | End Date: " + saved.getEndDate() + " (dünən)");
            }
        } catch (Exception e) {
            log.error("❌ Expired subscription xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
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
                    "📊 Subscription Statistikası:\n" +
                            "Toplam: %d\n" +
                            "✅ Aktiv: %d\n" +
                            "❌ Bitmiş: %d\n" +
                            "🚫 Ləğv edilmiş: %d",
                    total, active, expired, cancelled
            ));
        } catch (Exception e) {
            log.error("❌ Statistika xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
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
                log.info("🗑️ Test subscription silindi: ID={}", sub.getId());
            }

            return ResponseEntity.ok("✅ " + deletedCount + " test subscription silindi");
        } catch (Exception e) {
            log.error("❌ Cleanup xətası", e);
            return ResponseEntity.status(500).body("❌ Xəta: " + e.getMessage());
        }
    }
}