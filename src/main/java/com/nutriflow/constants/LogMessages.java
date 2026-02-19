package com.nutriflow.constants;

import lombok.experimental.UtilityClass;

/**
 * Constants for messages used in Activity Log.
 * Prepared for information (i18n) purposes.
 *
 * Usage:
 * activityLogService.logAction(..., LogMessages.DIETITIAN_CREATED);
 */
@UtilityClass
public class LogMessages {

    // ============= DIETITIAN MESSAGES =============
    public static final String DIETITIAN_CREATED = "New dietitian added to the system";
    public static final String DIETITIAN_UPDATED = "Dietitian information updated";
    public static final String DIETITIAN_DELETED = "Dietitian deleted";
    public static final String DIETITIAN_ACTIVATED = "Dietitian activated";
    public static final String DIETITIAN_DEACTIVATED = "Dietitian deactivated";

    // ============= CATERER MESSAGES =============
    public static final String CATERER_CREATED = "New caterer created";
    public static final String CATERER_UPDATED = "Caterer information updated";
    public static final String CATERER_DELETED = "Caterer deleted";
    public static final String CATERER_ACTIVATED = "Caterer activated";
    public static final String CATERER_DEACTIVATED = "Caterer deactivated";

    // ============= USER MESSAGES =============
    public static final String USER_CREATED = "New user created";
    public static final String USER_UPDATED = "User information updated";
    public static final String USER_DELETED = "User deleted";
    public static final String DIETITIAN_ASSIGNED = "Dietitian assigned to user";
    public static final String DIETITIAN_REMOVED = "Dietitian removed from user";
    public static final String CATERER_ASSIGNED = "Caterer assigned to user";
    public static final String CATERER_REMOVED = "Caterer removed from user";

    // ============= ADMIN MESSAGES =============
    public static final String ADMIN_CREATED = "New admin created";
    public static final String ADMIN_PROFILE_UPDATED = "Admin updated their own profile information";
    public static final String ADMIN_DELETED = "Sub-admin completely removed from the system";
    public static final String ADMIN_ACTIVATED = "Admin activated";
    public static final String ADMIN_DEACTIVATED = "Admin deactivated";

    // ============= PAYMENT MESSAGES =============
    public static final String PAYMENT_VERIFIED = "Payment verified";
    public static final String PAYMENT_REJECTED = "Payment rejected";
    public static final String PAYMENT_REFUNDED = "Payment refunded";

    // ============= MENU MESSAGES =============
    public static final String MENU_CREATED = "New menu created";
    public static final String MENU_UPDATED = "Menu updated";
    public static final String MENU_APPROVED = "Menu approved";
    public static final String MENU_REJECTED = "Menu rejected";

    // ============= SUBSCRIPTION MESSAGES =============
    public static final String SUBSCRIPTION_CREATED = "New subscription created";
    public static final String SUBSCRIPTION_CANCELLED = "Subscription cancelled";
    public static final String SUBSCRIPTION_PAUSED = "Subscription paused";
    public static final String SUBSCRIPTION_RESUMED = "Subscription resumed";

    // ============= GENERAL MESSAGES =============
    public static final String NEW_RECORD = "NEW RECORD";
    public static final String DELETED = "DELETED";
    public static final String NO_DATA = "No data available";

}