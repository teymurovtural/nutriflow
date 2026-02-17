package com.nutriflow.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Redis cache təmizlik scheduled task-ları
 *
 * Expired və ya istifadə olunmayan cache məlumatlarını təmizləyir.
 */
@Component
@Slf4j
public class RedisCleanupScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${nutriflow.redis.prefix.otp:OTP:}")
    private String otpPrefix;

    @Value("${nutriflow.redis.prefix.refresh-token:RT:}")
    private String refreshTokenPrefix;

    // @Qualifier ilə hansı bean istifadə edəcəyini göstəririk
    public RedisCleanupScheduler(@Qualifier("objectRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Expired OTP-ləri təmizləyir
     *
     * Schedule: Hər saat başı (məs: 01:00, 02:00, 03:00...)
     *
     * NOT: Redis TTL özü expire edir, amma manual cleanup yaxşı practice-dir
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredOtps() {
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🗑️ [REDIS-CLEANUP] OTP təmizliyi başladı");

        try {
            Set<String> otpKeys = redisTemplate.keys(otpPrefix + "*");

            if (otpKeys == null || otpKeys.isEmpty()) {
                log.info("✅ [REDIS-CLEANUP] Təmizlənəcək OTP tapılmadı");
                return;
            }

            int expiredCount = 0;
            for (String key : otpKeys) {
                Long ttl = redisTemplate.getExpire(key);

                // Əgər TTL -2 (key yoxdur) və ya -1 (TTL set olunmayıb) olarsa
                if (ttl != null && ttl < 0) {
                    redisTemplate.delete(key);
                    expiredCount++;
                }
            }

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [REDIS-CLEANUP] OTP təmizliyi tamamlandı | Silinən: {} | Müddət: {}ms",
                    expiredCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [REDIS-CLEANUP] OTP təmizliyi zamanı xəta: {}", e.getMessage(), e);
        }
    }

    /**
     * Expired refresh token-ləri təmizləyir
     *
     * Schedule: Hər gün saat 04:00-da
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExpiredRefreshTokens() {
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🗑️ [REDIS-CLEANUP] Refresh Token təmizliyi başladı");

        try {
            Set<String> tokenKeys = redisTemplate.keys(refreshTokenPrefix + "*");

            if (tokenKeys == null || tokenKeys.isEmpty()) {
                log.info("✅ [REDIS-CLEANUP] Təmizlənəcək token tapılmadı");
                return;
            }

            int expiredCount = 0;
            for (String key : tokenKeys) {
                Long ttl = redisTemplate.getExpire(key);

                if (ttl != null && ttl < 0) {
                    redisTemplate.delete(key);
                    expiredCount++;
                }
            }

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [REDIS-CLEANUP] Refresh Token təmizliyi tamamlandı | Silinən: {} | Müddət: {}ms",
                    expiredCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [REDIS-CLEANUP] Token təmizliyi zamanı xəta: {}", e.getMessage(), e);
        }
    }

    /**
     * Redis memory usage statistikası
     *
     * Schedule: Hər 6 saatda bir
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void logRedisStatistics() {
        log.info("📊 [REDIS-STATS] Redis statistikası yoxlanılır...");

        try {
            Set<String> allOtpKeys = redisTemplate.keys(otpPrefix + "*");
            Set<String> allTokenKeys = redisTemplate.keys(refreshTokenPrefix + "*");

            int otpCount = (allOtpKeys != null) ? allOtpKeys.size() : 0;
            int tokenCount = (allTokenKeys != null) ? allTokenKeys.size() : 0;

            log.info("📊 [REDIS-STATS] OTP Keys: {} | Token Keys: {} | Total: {}",
                    otpCount, tokenCount, otpCount + tokenCount);

        } catch (Exception e) {
            log.error("❌ [REDIS-STATS] Statistika xətası: {}", e.getMessage(), e);
        }
    }
}