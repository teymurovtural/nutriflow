package com.nutriflow.constants;

import lombok.experimental.UtilityClass;

/**
 * Activity Log-da istifadə olunan mesajların konstantları.
 * Məlumatlandırma (i18n) üçün hazırlanmışdır.
 *
 * İstifadə:
 * activityLogService.logAction(..., LogMessages.DIETITIAN_CREATED);
 */
@UtilityClass
public class LogMessages {

    // ============= DIETITIAN MESSAGES =============
    public static final String DIETITIAN_CREATED = "Yeni dietoloq sistemə əlavə edildi";
    public static final String DIETITIAN_UPDATED = "Dietoloq məlumatları yeniləndi";
    public static final String DIETITIAN_DELETED = "Dietoloq silindi";
    public static final String DIETITIAN_ACTIVATED = "Dietoloq aktivləşdirildi";
    public static final String DIETITIAN_DEACTIVATED = "Dietoloq deaktivləşdirildi";

    // ============= CATERER MESSAGES =============
    public static final String CATERER_CREATED = "Yeni mətbəx yaradıldı";
    public static final String CATERER_UPDATED = "Mətbəx məlumatları yeniləndi";
    public static final String CATERER_DELETED = "Mətbəx silindi";
    public static final String CATERER_ACTIVATED = "Mətbəx aktivləşdirildi";
    public static final String CATERER_DEACTIVATED = "Mətbəx deaktivləşdirildi";

    // ============= USER MESSAGES =============
    public static final String USER_CREATED = "Yeni istifadəçi yaradıldı";
    public static final String USER_UPDATED = "İstifadəçi məlumatları yeniləndi";
    public static final String USER_DELETED = "İstifadəçi silindi";
    public static final String DIETITIAN_ASSIGNED = "Dietoloq istifadəçiyə təyin edildi";
    public static final String DIETITIAN_REMOVED = "Dietoloq istifadəçidən ayrıldı";
    public static final String CATERER_ASSIGNED = "Mətbəx istifadəçiyə təyin edildi";
    public static final String CATERER_REMOVED = "Mətbəx istifadəçidən ayrıldı";

    // ============= ADMIN MESSAGES =============
    public static final String ADMIN_CREATED = "Yeni admin yaradıldı";
    public static final String ADMIN_PROFILE_UPDATED = "Admin öz profil məlumatlarını yenilədi";
    public static final String ADMIN_DELETED = "Sub-admin sistemdən tamamilə silindi";
    public static final String ADMIN_ACTIVATED = "Admin aktivləşdirildi";
    public static final String ADMIN_DEACTIVATED = "Admin deaktivləşdirildi";

    // ============= PAYMENT MESSAGES =============
    public static final String PAYMENT_VERIFIED = "Ödəniş təsdiqləndi";
    public static final String PAYMENT_REJECTED = "Ödəniş rədd edildi";
    public static final String PAYMENT_REFUNDED = "Ödənişə geri qaytarma edildi";

    // ============= MENU MESSAGES =============
    public static final String MENU_CREATED = "Yeni menyu yaradıldı";
    public static final String MENU_UPDATED = "Menyu yeniləndi";
    public static final String MENU_APPROVED = "Menyu təsdiqləndi";
    public static final String MENU_REJECTED = "Menyu rədd edildi";

    // ============= SUBSCRIPTION MESSAGES =============
    public static final String SUBSCRIPTION_CREATED = "Yeni abonəlik yaradıldı";
    public static final String SUBSCRIPTION_CANCELLED = "Abonəlik ləğv edildi";
    public static final String SUBSCRIPTION_PAUSED = "Abonəlik dəngələndi";
    public static final String SUBSCRIPTION_RESUMED = "Abonəlik dəvam etdirildi";

    // ============= GENERAL MESSAGES =============
    public static final String NEW_RECORD = "YENİ QEYDİYYAT";
    public static final String DELETED = "SİLİNDİ";
    public static final String NO_DATA = "Məlumat yoxdur";
}