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
 * Scheduled tasks for Redis cache cleanup.
 *
 * Cleans up expired or unused cache data.
 */
@Component
@Slf4j
public class RedisCleanupScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${nutriflow.redis.prefix.otp:OTP:}")
    private String otpPrefix;

    @Value("${nutriflow.redis.prefix.refresh-token:RT:}")
    private String refreshTokenPrefix;

    // Using @Qualifier to specify which bean to use
    public RedisCleanupScheduler(@Qualifier("objectRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Cleans up expired OTPs
     *
     * Schedule: At the start of every hour (e.g. 01:00, 02:00, 03:00...)
     *
     * NOTE: Redis TTL handles expiry on its own, but manual cleanup is good practice
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredOtps() {
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🗑️ [REDIS-CLEANUP] OTP cleanup started");

        try {
            Set<String> otpKeys = redisTemplate.keys(otpPrefix + "*");

            if (otpKeys == null || otpKeys.isEmpty()) {
                log.info("✅ [REDIS-CLEANUP] No OTPs found to clean up");
                return;
            }

            int expiredCount = 0;
            for (String key : otpKeys) {
                Long ttl = redisTemplate.getExpire(key);

                // If TTL is -2 (key does not exist) or -1 (TTL not set)
                if (ttl != null && ttl < 0) {
                    redisTemplate.delete(key);
                    expiredCount++;
                }
            }

            long durationMs = java.time.Duration.between(startTime, LocalDateTime.now()).toMillis();

            log.info("✅ [REDIS-CLEANUP] OTP cleanup completed | Deleted: {} | Duration: {}ms",
                    expiredCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [REDIS-CLEANUP] Error during OTP cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleans up expired refresh tokens
     *
     * Schedule: Every day at 04:00
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanupExpiredRefreshTokens() {
        LocalDateTime startTime = LocalDateTime.now();

        log.info("🗑️ [REDIS-CLEANUP] Refresh Token cleanup started");

        try {
            Set<String> tokenKeys = redisTemplate.keys(refreshTokenPrefix + "*");

            if (tokenKeys == null || tokenKeys.isEmpty()) {
                log.info("✅ [REDIS-CLEANUP] No tokens found to clean up");
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

            log.info("✅ [REDIS-CLEANUP] Refresh Token cleanup completed | Deleted: {} | Duration: {}ms",
                    expiredCount, durationMs);

        } catch (Exception e) {
            log.error("❌ [REDIS-CLEANUP] Error during token cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Redis memory usage statistics
     *
     * Schedule: Every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * ?")
    public void logRedisStatistics() {
        log.info("📊 [REDIS-STATS] Checking Redis statistics...");

        try {
            Set<String> allOtpKeys = redisTemplate.keys(otpPrefix + "*");
            Set<String> allTokenKeys = redisTemplate.keys(refreshTokenPrefix + "*");

            int otpCount = (allOtpKeys != null) ? allOtpKeys.size() : 0;
            int tokenCount = (allTokenKeys != null) ? allTokenKeys.size() : 0;

            log.info("📊 [REDIS-STATS] OTP Keys: {} | Token Keys: {} | Total: {}",
                    otpCount, tokenCount, otpCount + tokenCount);

        } catch (Exception e) {
            log.error("❌ [REDIS-STATS] Statistics error: {}", e.getMessage(), e);
        }
    }
}