package com.nutriflow.services.impl;

import com.nutriflow.entities.ActivityLogEntity;
import com.nutriflow.enums.Role;
import com.nutriflow.repositories.ActivityLogRepository;
import com.nutriflow.services.ActivityLogService;
import com.nutriflow.utils.IpAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activity Log Service Implementation.
 * İstifadəçi və admin əməliyyatlarını bazada saxlayır.
 *
 * NOTE: @Transactional(propagation = Propagation.REQUIRES_NEW)
 * istifadə edilir ki, loqlama əməliyyatı əsas tranzaksiya ilə asılı olmasın.
 * Əgər əsas əməliyyat fail olsa belə, loq yaradılır.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final IpAddressUtil ipAddressUtil;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(Role role, Long actorId, String action, String entityType, Long entityId,
                          String oldValue, String newValue, String details) {

        try {
            ActivityLogEntity logEntry = ActivityLogEntity.builder()
                    .role(role)
                    .actorId(actorId)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    // Null gələrsə, bazada null göründümsün deyə default dəyər veririk
                    .oldValue(oldValue != null ? oldValue : "Məlumat yoxdur")
                    .newValue(newValue != null ? newValue : "Məlumat yoxdur")
                    .details(details)
                    .ipAddress(getClientIp())
                    .build();

            activityLogRepository.save(logEntry);

        } catch (Exception e) {
            // Log yazarkən xəta olsa, sistemin çökmməməsi üçün sadəcə konsola yazırıq
            log.error("Activity Log yadda saxlanılarkən xəta baş verdi: {}", e.getMessage(), e);
        }
    }

    /**
     * Client-in real IP ünvanını tapır.
     * IpAddressUtil-dən istifadə edir.
     *
     * @return Client IP ünvanı
     */
    @Override
    public String getClientIp() {
        return ipAddressUtil.getClientIp();
    }
}