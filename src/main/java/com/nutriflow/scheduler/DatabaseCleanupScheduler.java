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
     * Köhnə çatdırılma qeydlərini silir
     *
     * Schedule: Hər ayın 1-də saat 03:00-da
     * Silir: 1 ildən köhnə delivery qeydləri
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional
    public void cleanupOldDeliveries() {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🗑️ [CLEANUP] Köhnə çatdırılma təmizliyi başladı | Limit tarixi: {}", oneYearAgo);

        try {
            int deletedCount = deliveryRepository.deleteOldDeliveries(oneYearAgo);

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [CLEANUP] Çatdırılma təmizliyi tamamlandı | Silinən qeyd: {} | Müddət: {}ms",
                    deletedCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [CLEANUP] Təmizlik zamanı xəta baş verdi: {}", e.getMessage(), e);
            // TODO: Admin-ə notification göndər
        }
    }

    /**
     * Database statistikası log edir (monitoring üçün)
     *
     * Schedule: Hər gün saat 02:00-da
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional(readOnly = true)
    public void logDatabaseStatistics() {
        log.info("📊 [STATS] Database statistikası yoxlanılır...");

        try {
            long totalDeliveries = deliveryRepository.count();
            // Digər repository count-lar əlavə et

            log.info("📊 [STATS] Total Deliveries: {}", totalDeliveries);

        } catch (Exception e) {
            log.error("❌ [STATS] Statistika yoxlanılarkən xəta: {}", e.getMessage(), e);
        }
    }
}