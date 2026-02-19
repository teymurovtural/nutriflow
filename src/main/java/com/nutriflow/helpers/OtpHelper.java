package com.nutriflow.helpers;

import com.nutriflow.entities.OtpEntity;
import com.nutriflow.exceptions.InvalidOtpException;
import com.nutriflow.repositories.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for OTP operations.
 * OTP generation, saving, validation and cleanup logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OtpHelper {

    private final OtpRepository otpRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Generates a secure 6-digit OTP.
     *
     * @return OTP string
     */
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Creates a new OTP, saves it to Redis and DB.
     * Invalidates any previous OTP for the same email.
     *
     * @param email     User email
     * @param otpPrefix Redis key prefix
     * @return Generated OTP
     */
    @Transactional
    public String createAndSaveOtp(String email, String otpPrefix) {
        log.info("Creating new OTP for: {}", email);

        // 1. Köhnə OTP-ni Redis-dən sil
        redisTemplate.delete(otpPrefix + email);

        // 2. Köhnə OTP-ni DB-də used et
        otpRepository.findLatestByEmail(email).ifPresent(otp -> {
            otp.setUsed(true);
            otpRepository.save(otp);
            log.info("Old OTP marked as used: {}", email);
        });

        // 3. Yeni OTP yarat
        String otp = generateOtp();

        // 4. Redis-ə yaz
        redisTemplate.opsForValue().set(otpPrefix + email, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
        log.info("OTP written to Redis (TTL={}min): {}", OTP_EXPIRY_MINUTES, email);

        // 5. DB-yə yaz
        OtpEntity otpEntity = OtpEntity.builder()
                .email(email)
                .code(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .isUsed(false)
                .build();
        otpRepository.save(otpEntity);
        log.info("OTP saved to DB: {}", email);

        return otp;
    }

    /**
     * Validates OTP from Redis.
     * Throws exception if invalid or expired.
     *
     * @param email     User email
     * @param otpCode   OTP entered by user
     * @param otpPrefix Redis key prefix
     */
    public void validateOtp(String email, String otpCode, String otpPrefix) {
        log.info("Validating OTP for: {}", email);

        String storedOtp = redisTemplate.opsForValue().get(otpPrefix + email);

        if (storedOtp == null) {
            log.warn("OTP not found in Redis (may have expired): {}", email);
            throw new InvalidOtpException("Verification code is invalid or has expired.");
        }

        if (!storedOtp.equals(otpCode)) {
            log.warn("Incorrect OTP entered - Email: {}", email);
            throw new InvalidOtpException("Incorrect verification code.");
        }

        log.info("OTP validated successfully: {}", email);
    }

    /**
     * Marks OTP as used in DB and deletes from Redis.
     *
     * @param email     User email
     * @param otpCode   OTP code
     * @param otpPrefix Redis key prefix
     */
    @Transactional
    public void markOtpAsUsed(String email, String otpCode, String otpPrefix) {
        // DB-də used et
        otpRepository.findByEmailAndCode(email, otpCode).ifPresent(otp -> {
            otp.setUsed(true);
            otpRepository.save(otp);
            log.info("OTP marked as used in DB: {}", email);
        });

        // Redis-dən sil
        redisTemplate.delete(otpPrefix + email);
        log.info("OTP deleted from Redis: {}", email);
    }
}