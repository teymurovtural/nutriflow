package com.nutriflow.scheduler;

import com.nutriflow.repositories.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseCleanupScheduler {

    private final DeliveryRepository deliveryRepository;

    /**
     * Deletes old delivery records
     *
     * Schedule: On the 1st of every month at 03:00
     * Deletes: Delivery records older than 1 year
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional
    public void cleanupOldDeliveries() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🗑️ [CLEANUP] Old delivery cleanup started | Cutoff date: {}", oneYearAgo);

        try {
            int deletedCount = deliveryRepository.deleteOldDeliveries(oneYearAgo);

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [CLEANUP] Delivery cleanup completed | Deleted records: {} | Duration: {}ms",
                    deletedCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [CLEANUP] Error occurred during cleanup: {}", e.getMessage(), e);
            // TODO: Send notification to admin
        }
    }

    /**
     * Logs database statistics (for monitoring)
     *
     * Schedule: Every day at 02:00
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(readOnly = true)
    public void logDatabaseStatistics() {
        log.info("📊 [STATS] Checking database statistics...");

        try {
            long totalDeliveries = deliveryRepository.count();
            // Add other repository counts here

            log.info("📊 [STATS] Total Deliveries: {}", totalDeliveries);

        } catch (Exception e) {
            log.error("❌ [STATS] Error while checking statistics: {}", e.getMessage(), e);
        }
    }
}