package com.nutriflow.utils;

import com.nutriflow.entities.*;
import lombok.experimental.UtilityClass;

/**
 * Utility class for formatting data used in activity logs.
 * Separates string formatting logic from services.
 *
 * Usage:
 * String oldData = LoggingUtil.formatDietitianData(dietitian);
 */
@UtilityClass
public class LoggingUtils {

    // ============= DIETITIAN =============

    /**
     * Formats dietitian data for logging
     */
    public String formatDietitianData(DietitianEntity entity) {
        if (entity == null) {
            return "No data available";
        }
        return String.format(
                "Name: %s %s, Email: %s, Role: %s, Status: %s",
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRole(),
                entity.isActive() ? "ACTIVE" : "INACTIVE"
        );
    }

    // ============= CATERER =============

    /**
     * Formats caterer data for logging
     */
    public String formatCatererData(CatererEntity entity) {
        if (entity == null) {
            return "No data available";
        }
        return String.format(
                "Kitchen: %s, Email: %s, Role: %s, Status: %s",
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getStatus()
        );
    }

    // ============= USER =============

    /**
     * Formats user data for logging
     */
    public String formatUserData(UserEntity entity) {
        if (entity == null) {
            return "No data available";
        }
        return String.format(
                "Name: %s %s, Email: %s, Role: %s, Status: %s",
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getStatus()
        );
    }

    // ============= ADMIN =============

    /**
     * Formats admin data for logging
     */
    public String formatAdminData(AdminEntity entity) {
        if (entity == null) {
            return "No data available";
        }
        return String.format(
                "Name: %s %s, Email: %s, Role: ADMIN, Status: %s",
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.isActive() ? "ACTIVE" : "INACTIVE"
        );
    }

    // ============= PAYMENT =============

    /**
     * Formats payment data for logging
     */
    public String formatPaymentData(PaymentEntity entity) {
        if (entity == null) {
            return "No data available";
        }
        return String.format(
                "Amount: %.2f %s, Status: %s, Date: %s",
                entity.getAmount(),
                "AZN",
                entity.getStatus(),
                entity.getPaymentDate()
        );
    }

    // ============= DELETION =============

    /**
     * Constant message used in delete operations
     */
    public String deletedMessage() {
        return "DELETED";
    }

    // ============= NEW RECORD =============

    /**
     * Constant message used when a new record is created
     */
    public String newRecordMessage() {
        return "NEW RECORD";
    }
}