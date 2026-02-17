package com.nutriflow.services;

import com.nutriflow.enums.Role;

/**
 * Activity Log Service Interface.
 * İstifadəçi və admin əməliyyatlarını bazada saxlamaq məsuliyyəti.
 */
public interface ActivityLogService {


    void logAction(Role role, Long actorId, String action, String entityType, Long entityId,
                   String oldValue, String newValue, String details);

    /**
     * Client-in real IP ünvanını tapır.
     * Proxy və Load Balancer-ləri nəzərə alır.
     *
     * @return Client IP ünvanı
     */
    String getClientIp();
}