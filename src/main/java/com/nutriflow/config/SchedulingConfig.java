package com.nutriflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling konfiqurasiyası
 *
 * @EnableScheduling - Spring-də scheduled task-ları aktivləşdirir
 * Bu konfiqurasiya ilə @Scheduled annotation-lı metodlar avtomatik işləyəcək
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Konfiqurasiya annotation ilə hazırdır
}