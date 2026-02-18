package com.nutriflow.utils;

import com.nutriflow.entities.*;
import com.nutriflow.enums.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for working with entity objects.
 * Entity data extraction, null checks, etc.
 */
@Slf4j
public class EntityUtils {

    /**
     * Returns the full name of a user.
     *
     * @param user User entity
     * @return Full name (First name + Last name)
     */
    public static String getUserFullName(UserEntity user) {
        if (user == null) {
            return "No data available";
        }
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * Returns the full name of a dietitian.
     *
     * @param dietitian Dietitian entity
     * @return Full name
     */
    public static String getDietitianFullName(DietitianEntity dietitian) {
        if (dietitian == null) {
            return "Not assigned";
        }
        String firstName = dietitian.getFirstName() != null ? dietitian.getFirstName() : "";
        String lastName = dietitian.getLastName() != null ? dietitian.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * Returns the full name of an admin.
     *
     * @param admin Admin entity
     * @return Full name
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
     * Returns the full address of a user.
     *
     * @param address Address entity
     * @return Formatted address
     */
    public static String getFullAddress(AddressEntity address) {
        if (address == null) {
            return "No address information available";
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

        return fullAddress.length() > 0 ? fullAddress.toString() : "No address information available";
    }

    /**
     * Checks if a user is active.
     *
     * @param user User entity
     * @return true if the user is active
     */
    public static boolean isUserActive(UserEntity user) {
        return user != null && user.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Checks if a subscription is active.
     *
     * @param subscription Subscription entity
     * @return true if the subscription is active
     */
    public static boolean isSubscriptionActive(SubscriptionEntity subscription) {
        return subscription != null && subscription.getStatus() == SubscriptionStatus.ACTIVE;
    }

    /**
     * Checks if a delivery is completed.
     *
     * @param delivery Delivery entity
     * @return true if the delivery is completed
     */
    public static boolean isDeliveryCompleted(DeliveryEntity delivery) {
        return delivery != null && delivery.getStatus() == DeliveryStatus.DELIVERED;
    }

    /**
     * Checks if a menu batch is approved.
     *
     * @param batch MenuBatch entity
     * @return true if the menu batch is approved
     */
    public static boolean isMenuApproved(MenuBatchEntity batch) {
        return batch != null && batch.getStatus() == MenuStatus.APPROVED;
    }

    /**
     * Checks if a payment is successful.
     *
     * @param payment Payment entity
     * @return true if the payment is successful
     */
    public static boolean isPaymentSuccessful(PaymentEntity payment) {
        return payment != null && payment.getStatus() == PaymentStatus.SUCCESS;
    }

    /**
     * Calculates BMI from a health profile.
     *
     * @param healthProfile HealthProfile entity
     * @return BMI value
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
        return Math.round(bmi * 10.0) / 10.0;
    }

    /**
     * Checks if a user's email is verified.
     *
     * @param user User entity
     * @return true if the email is verified
     */
    public static boolean isEmailVerified(UserEntity user) {
        return user != null && user.isEmailVerified();
    }

    /**
     * Checks if a dietitian is active.
     *
     * @param dietitian Dietitian entity
     * @return true if the dietitian is active
     */
    public static boolean isDietitianActive(DietitianEntity dietitian) {
        return dietitian != null && dietitian.isActive();
    }

    /**
     * Checks if a caterer is active.
     *
     * @param caterer Caterer entity
     * @return true if the caterer is active
     */
    public static boolean isCatererActive(CatererEntity caterer) {
        return caterer != null && caterer.getStatus() == CatererStatus.ACTIVE;
    }

    /**
     * Extracts menu items for a specific day from a menu batch.
     *
     * @param batch MenuBatch entity
     * @param day   Day number
     * @return Menu items for the given day
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
     * Returns the active subscription of a user.
     *
     * @param user User entity
     * @return Active subscription or null
     */
    public static SubscriptionEntity getActiveSubscription(UserEntity user) {
        if (user == null || user.getSubscription() == null) {
            return null;
        }

        SubscriptionEntity subscription = user.getSubscription();
        return isSubscriptionActive(subscription) ? subscription : null;
    }

    /**
     * Returns the plan name of a subscription.
     *
     * @param subscription Subscription entity
     * @return Plan name
     */
    public static String getPlanName(SubscriptionEntity subscription) {
        if (subscription == null || subscription.getPlanName() == null) {
            return "No plan available";
        }
        return subscription.getPlanName();
    }

    /**
     * Returns the number of medical files.
     *
     * @param healthProfile HealthProfile entity
     * @return Number of files
     */
    public static int getMedicalFileCount(HealthProfileEntity healthProfile) {
        if (healthProfile == null || healthProfile.getMedicalFiles() == null) {
            return 0;
        }
        return healthProfile.getMedicalFiles().size();
    }

    /**
     * Checks if a user has a health profile.
     *
     * @param user User entity
     * @return true if the user has a health profile
     */
    public static boolean hasHealthProfile(UserEntity user) {
        return user != null && user.getHealthProfile() != null;
    }

    /**
     * Checks if a user has an address.
     *
     * @param user User entity
     * @return true if the user has an address
     */
    public static boolean hasAddress(UserEntity user) {
        return user != null && user.getAddress() != null;
    }

    /**
     * Safely converts an enum to a String.
     *
     * @param enumValue Enum value
     * @return String representation or "N/A"
     */
    public static String enumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : "N/A";
    }

    /**
     * Checks if a user has an assigned dietitian.
     *
     * @param user User entity
     * @return true if a dietitian is assigned
     */
    public static boolean hasDietitian(UserEntity user) {
        return user != null && user.getDietitian() != null;
    }

    /**
     * Checks if a user has an assigned caterer.
     *
     * @param user User entity
     * @return true if a caterer is assigned
     */
    public static boolean hasCaterer(UserEntity user) {
        return user != null && user.getCaterer() != null;
    }
}