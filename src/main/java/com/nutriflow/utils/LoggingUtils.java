package com.nutriflow.utils;

import com.nutriflow.entities.*;
import lombok.experimental.UtilityClass;

/**
 * Fəaliyyət loqlarında istifadə olunan məlumatları formatlayan utility sınıfı.
 * Service-lərdən string formatting loqikasını ayırır.
 *
 * İstifadə:
 * String oldData = LoggingUtil.formatDietitianData(dietitian);
 */
@UtilityClass
public class LoggingUtils {

    // ============= DIETITIAN =============

    /**
     * Dietitian məlumatlarını loqlama üçün formatlar
     */
    public String formatDietitianData(DietitianEntity entity) {
        if (entity == null) {
            return "Məlumat yoxdur";
        }
        return String.format(
                "Ad: %s %s, Email: %s, Rol: %s, Status: %s",
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRole(),
                entity.isActive() ? "AKTİV" : "DEAKTİV"
        );
    }

    // ============= CATERER =============

    /**
     * Caterer məlumatlarını loqlama üçün formatlar
     */
    public String formatCatererData(CatererEntity entity) {
        if (entity == null) {
            return "Məlumat yoxdur";
        }
        return String.format(
                "Mətbəx: %s, Email: %s, Rol: %s, Status: %s",
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getStatus()
        );
    }

    // ============= USER =============

    /**
     * User məlumatlarını loqlama üçün formatlar
     */
    public String formatUserData(UserEntity entity) {
        if (entity == null) {
            return "Məlumat yoxdur";
        }
        return String.format(
                "Ad: %s %s, Email: %s, Rol: %s, Status: %s",
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getRole(),
                entity.getStatus()
        );
    }

    // ============= ADMIN =============

    /**
     * Admin məlumatlarını loqlama üçün formatlar
     */
    public String formatAdminData(AdminEntity entity) {
        if (entity == null) {
            return "Məlumat yoxdur";
        }
        return String.format(
                "Ad: %s %s, Email: %s, Rol: ADMIN, Status: %s",
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.isActive() ? "AKTİV" : "DEAKTİV"
        );
    }

    // ============= PAYMENT =============

    /**
     * Payment məlumatlarını loqlama üçün formatlar
     */
    public String formatPaymentData(PaymentEntity entity) {
        if (entity == null) {
            return "Məlumat yoxdur";
        }
        return String.format(
                "Məbləğ: %.2f %s, Status: %s, Tarix: %s",
                entity.getAmount(),
                "AZN",
                entity.getStatus(),
                entity.getPaymentDate()
        );
    }

    // ============= DELETION =============

    /**
     * Silmə əməliyyatında istifadə olunan sabit mətn
     */
    public String deletedMessage() {
        return "SİLİNDİ";
    }

    // ============= NEW RECORD =============

    /**
     * Yeni qeyd yaradılması üçün sabit mətn
     */
    public String newRecordMessage() {
        return "YENİ QEYDİYYAT";
    }
}