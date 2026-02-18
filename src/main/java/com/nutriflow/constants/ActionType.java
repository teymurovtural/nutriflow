package com.nutriflow.constants;

import lombok.experimental.UtilityClass;

/**
 * Constants for action types used in the Activity Log.
 * Use constants instead of hardcoded strings.
 *
 * Usage:
 * activityLogService.logAction(Role.ADMIN, userId, ActionType.CREATE_DIETITIAN, ...);
 */
@UtilityClass
public class ActionType {

    // ============= DIETITIAN OPERATIONS =============
    public static final String CREATE_DIETITIAN = "CREATE_DIETITIAN";
    public static final String UPDATE_DIETITIAN = "UPDATE_DIETITIAN";
    public static final String DELETE_DIETITIAN = "DELETE_DIETITIAN";
    public static final String ACTIVATE_DIETITIAN = "ACTIVATE_DIETITIAN";
    public static final String DEACTIVATE_DIETITIAN = "DEACTIVATE_DIETITIAN";

    // ============= CATERER OPERATIONS =============
    public static final String CREATE_CATERER = "CREATE_CATERER";
    public static final String UPDATE_CATERER_ADMIN = "UPDATE_CATERER_ADMIN";
    public static final String DELETE_CATERER = "DELETE_CATERER";
    public static final String ACTIVATE_CATERER = "ACTIVATE_CATERER";
    public static final String DEACTIVATE_CATERER = "DEACTIVATE_CATERER";

    // ============= USER OPERATIONS =============
    public static final String CREATE_USER = "CREATE_USER";
    public static final String UPDATE_USER = "UPDATE_USER";
    public static final String DELETE_USER = "DELETE_USER";
    public static final String ASSIGN_DIETITIAN = "ASSIGN_DIETITIAN";
    public static final String REMOVE_DIETITIAN = "REMOVE_DIETITIAN";
    public static final String ASSIGN_CATERER = "ASSIGN_CATERER";
    public static final String REMOVE_CATERER = "REMOVE_CATERER";

    // ============= ADMIN OPERATIONS =============
    public static final String CREATE_ADMIN = "CREATE_ADMIN";
    public static final String UPDATE_PROFILE = "UPDATE_PROFILE";
    public static final String DELETE_SUB_ADMIN = "DELETE_SUB_ADMIN";
    public static final String DEACTIVATE_ADMIN = "DEACTIVATE_ADMIN";
    public static final String VIEW_DASHBOARD = "VIEW_DASHBOARD";
    public static final String ACTIVATE_ADMIN = "ACTIVATE_ADMIN";

    // ============= PAYMENT OPERATIONS =============
    public static final String VERIFY_PAYMENT = "VERIFY_PAYMENT";
    public static final String REJECT_PAYMENT = "REJECT_PAYMENT";
    public static final String REFUND_PAYMENT = "REFUND_PAYMENT";

    // ============= MENU OPERATIONS =============
    public static final String CREATE_MENU = "CREATE_MENU";
    public static final String UPDATE_MENU = "UPDATE_MENU";
    public static final String APPROVE_MENU = "APPROVE_MENU";
    public static final String REJECT_MENU = "REJECT_MENU";

    // ============= SUBSCRIPTION OPERATIONS =============
    public static final String CREATE_SUBSCRIPTION = "CREATE_SUBSCRIPTION";
    public static final String CANCEL_SUBSCRIPTION = "CANCEL_SUBSCRIPTION";
    public static final String PAUSE_SUBSCRIPTION = "PAUSE_SUBSCRIPTION";
    public static final String RESUME_SUBSCRIPTION = "RESUME_SUBSCRIPTION";
}