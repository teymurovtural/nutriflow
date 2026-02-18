package com.nutriflow.services;

import com.nutriflow.enums.Role;

/**
 * Activity Log Service Interface.
 * Responsible for saving user and admin operations to the database.
 */
public interface ActivityLogService {

    void logAction(Role role, Long actorId, String action, String entityType, Long entityId,
                   String oldValue, String newValue, String details);

    /**
     * Retrieves the real IP address of the client.
     * Takes Proxy and Load Balancers into account.
     *
     * @return Client IP address
     */
    String getClientIp();
}