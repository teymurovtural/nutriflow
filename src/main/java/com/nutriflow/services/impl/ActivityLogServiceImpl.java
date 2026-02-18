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
 * Saves user and admin operations to the database.
 *
 * NOTE: @Transactional(propagation = Propagation.REQUIRES_NEW)
 * is used so that the logging operation is independent of the main transaction.
 * Even if the main operation fails, the log entry is still created.
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
                    // If null is received, we assign a default value so it doesn't appear as null in the database
                    .oldValue(oldValue != null ? oldValue : "No data available")
                    .newValue(newValue != null ? newValue : "No data available")
                    .details(details)
                    .ipAddress(getClientIp())
                    .build();

            activityLogRepository.save(logEntry);

        } catch (Exception e) {
            // If an error occurs while writing the log, we only print to console to prevent system crash
            log.error("An error occurred while saving the Activity Log: {}", e.getMessage(), e);
        }
    }

    /**
     * Retrieves the real IP address of the client.
     * Uses IpAddressUtil.
     *
     * @return Client IP address
     */
    @Override
    public String getClientIp() {
        return ipAddressUtil.getClientIp();
    }
}