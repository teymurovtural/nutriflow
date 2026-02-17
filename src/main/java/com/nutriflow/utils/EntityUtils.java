package com.nutriflow.utils;

import com.nutriflow.entities.*;
import com.nutriflow.enums.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Entity obyektləri ilə işləmək üçün yardımçı sinif.
 * Entity-lərdən məlumat çıxarma, null check və s.
 */
@Slf4j
public class EntityUtils {

    /**
     * User-in tam adını qaytarır.
     *
     * @param user User entity
     * @return Tam ad (Ad + Soyad)
     */
    public static String getUserFullName(UserEntity user) {
        if (user == null) {
            return "Məlumat yoxdur";
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * Dietitian-ın tam adını qaytarır.
     *
     * @param dietitian Dietitian entity
     * @return Tam ad
     */
    public static String getDietitianFullName(DietitianEntity dietitian) {
        if (dietitian == null) {
            return "Təyin olunmayıb";
        }
        String firstName = dietitian.getFirstName() != null ? dietitian.getFirstName() : "";
        String lastName = dietitian.getLastName() != null ? dietitian.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * Admin-in tam adını qaytarır.
     *
     * @param admin Admin entity
     * @return Tam ad
     */
    public static String getAdminFullName(AdminEntity admin) {
        if (admin == null) {
            return "Admin";
        }
        String firstName = admin.getFirstName() != null ? admin.getFirstName() : "";
        String lastName = admin.getLastName() != null ? admin.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * User-in tam ünvanını qaytarır.
     *
     * @param address Address entity
     * @return Formatlanmış ünvan
     */
    public static String getFullAddress(AddressEntity address) {
        if (address == null) {
            return "Ünvan məlumatı yoxdur";
        }

        StringBuilder fullAddress = new StringBuilder();

        if (address.getAddressDetails() != null) {
            fullAddress.append(address.getAddressDetails());
        }

        if (address.getDistrict() != null) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(address.getDistrict());
        }

        if (address.getCity() != null) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(address.getCity());
        }

        return fullAddress.length() > 0 ? fullAddress.toString() : "Ünvan məlumatı yoxdur";
    }

    /**
     * User-in aktiv statusunu yoxlayır.
     *
     * @param user User entity
     * @return true əgər aktiv statusdadırsa
     */
    public static boolean isUserActive(UserEntity user) {
        return user != null && user.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Subscription-ın aktiv olub-olmadığını yoxlayır.
     *
     * @param subscription Subscription entity
     * @return true əgər aktivsə
     */
    public static boolean isSubscriptionActive(SubscriptionEntity subscription) {
        return subscription != null && subscription.getStatus() == SubscriptionStatus.ACTIVE;
    }

    /**
     * Delivery-nin tamamlanıb-tamamlanmadığını yoxlayır.
     *
     * @param delivery Delivery entity
     * @return true əgər tamamlanıbsa
     */
    public static boolean isDeliveryCompleted(DeliveryEntity delivery) {
        return delivery != null && delivery.getStatus() == DeliveryStatus.DELIVERED;
    }

    /**
     * Menu batch-in təsdiqlənib-təsdiqlənmədiyini yoxlayır.
     *
     * @param batch MenuBatch entity
     * @return true əgər təsdiqlənibsə
     */
    public static boolean isMenuApproved(MenuBatchEntity batch) {
        return batch != null && batch.getStatus() == MenuStatus.APPROVED;
    }

    /**
     * Payment-in uğurlu olub-olmadığını yoxlayır.
     *
     * @param payment Payment entity
     * @return true əgər uğurludursa
     */
    public static boolean isPaymentSuccessful(PaymentEntity payment) {
        return payment != null && payment.getStatus() == PaymentStatus.SUCCESS;
    }

    /**
     * HealthProfile-dan BMI hesablayır.
     *
     * @param healthProfile HealthProfile entity
     * @return BMI dəyəri
     */
    public static double calculateBMI(HealthProfileEntity healthProfile) {
        if (healthProfile == null || healthProfile.getHeight() == null || healthProfile.getWeight() == null) {
            return 0.0;
        }

        double heightInMeters = healthProfile.getHeight() / 100.0;
        if (heightInMeters <= 0) {
            return 0.0;
        }

        double bmi = healthProfile.getWeight() / (heightInMeters * heightInMeters);
        return Math.round(bmi * 10.0) / 10.0; // 1 onluq dəqiqliklə
    }

    /**
     * User-in email-inin təsdiqlənib-təsdiqlənmədiyini yoxlayır.
     *
     * @param user User entity
     * @return true əgər təsdiqlənibsə
     */
    public static boolean isEmailVerified(UserEntity user) {
        return user != null && user.isEmailVerified();
    }

    /**
     * Dietitian-ın aktiv olub-olmadığını yoxlayır.
     *
     * @param dietitian Dietitian entity
     * @return true əgər aktivsə
     */
    public static boolean isDietitianActive(DietitianEntity dietitian) {
        return dietitian != null && dietitian.isActive();
    }

    /**
     * Caterer-in aktiv olub-olmadığını yoxlayır.
     *
     * @param caterer Caterer entity
     * @return true əgər aktivsə
     */
    public static boolean isCatererActive(CatererEntity caterer) {
        return caterer != null && caterer.getStatus() == CatererStatus.ACTIVE;
    }

    /**
     * MenuBatch-dən müəyyən gün üçün yemək itemlərini çıxarır.
     *
     * @param batch MenuBatch entity
     * @param day   Gün
     * @return Həmin günün menu itemləri
     */
    public static List<MenuItemEntity> getMenuItemsByDay(MenuBatchEntity batch, Integer day) {
        if (batch == null || batch.getItems() == null || day == null) {
            return List.of();
        }

        return batch.getItems().stream()
                .filter(item -> day.equals(item.getDay()))
                .collect(Collectors.toList());
    }

    /**
     * User-in aktiv subscription-ını qaytarır.
     *
     * @param user User entity
     * @return Aktiv subscription və ya null
     */
    public static SubscriptionEntity getActiveSubscription(UserEntity user) {
        if (user == null || user.getSubscription() == null) {
            return null;
        }

        SubscriptionEntity subscription = user.getSubscription();
        return isSubscriptionActive(subscription) ? subscription : null;
    }

    /**
     * Subscription-ın plan adını qaytarır.
     *
     * @param subscription Subscription entity
     * @return Plan adı
     */
    public static String getPlanName(SubscriptionEntity subscription) {
        if (subscription == null || subscription.getPlanName() == null) {
            return "Plan yoxdur";
        }
        return subscription.getPlanName();
    }

    /**
     * Medical file-ların sayını qaytarır.
     *
     * @param healthProfile HealthProfile entity
     * @return File sayı
     */
    public static int getMedicalFileCount(HealthProfileEntity healthProfile) {
        if (healthProfile == null || healthProfile.getMedicalFiles() == null) {
            return 0;
        }
        return healthProfile.getMedicalFiles().size();
    }

    /**
     * User-in health profile-ına sahib olub-olmadığını yoxlayır.
     *
     * @param user User entity
     * @return true əgər profile varsa
     */
    public static boolean hasHealthProfile(UserEntity user) {
        return user != null && user.getHealthProfile() != null;
    }

    /**
     * User-in address-inə sahib olub-olmadığını yoxlayır.
     *
     * @param user User entity
     * @return true əgər address varsa
     */
    public static boolean hasAddress(UserEntity user) {
        return user != null && user.getAddress() != null;
    }

    /**
     * Enum-u safe şəkildə String-ə çevirir.
     *
     * @param enumValue Enum dəyəri
     * @return String representation və ya "N/A"
     */
    public static String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : "N/A";
    }

    /**
     * User-in dietitian-a təyin edilib-edilmədiyini yoxlayır.
     *
     * @param user User entity
     * @return true əgər dietitian təyin ediləcəksə
     */
    public static boolean hasDietitian(UserEntity user) {
        return user != null && user.getDietitian() != null;
    }

    /**
     * User-in caterer-ə təyin edilib-edilmədiyini yoxlayır.
     *
     * @param user User entity
     * @return true əgər caterer təyin ediləcəksə
     */
    public static boolean hasCaterer(UserEntity user) {
        return user != null && user.getCaterer() != null;
    }
}