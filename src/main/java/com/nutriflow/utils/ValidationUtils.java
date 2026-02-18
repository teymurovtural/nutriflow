package com.nutriflow.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * Validates email, phone, password, and other data fields.
 */
@Slf4j
public class ValidationUtils {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+994|0)(50|51|55|70|77|99)\\d{7}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    /**
     * Checks whether the email is in a valid format.
     *
     * @param email Email address
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Checks whether the phone number is in Azerbaijani format.
     * Format: +994XXXXXXXXX or 0XXXXXXXXX
     *
     * @param phone Phone number
     * @return true if valid
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("\\s", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Checks whether the password is strong.
     * Requirements:
     * - Minimum 8 characters
     * - At least 1 digit
     * - At least 1 lowercase letter
     * - At least 1 uppercase letter
     * - At least 1 special character (@#$%^&+=!)
     *
     * @param password Password
     * @return true if strong
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Checks whether the string is empty (null or blank).
     *
     * @param value Value to check
     * @return true if empty
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Checks whether the string is not empty.
     *
     * @param value Value to check
     * @return true if not empty
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * Checks whether the number is positive.
     *
     * @param number Number to check
     * @return true if positive
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }

    /**
     * Checks whether the number is non-negative (0 or greater).
     *
     * @param number Number to check
     * @return true if non-negative
     */
    public static boolean isNonNegative(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() >= 0;
    }

    /**
     * Checks whether the height value is within a logical range (100-250 cm).
     *
     * @param height Height (cm)
     * @return true if valid
     */
    public static boolean isValidHeight(Double height) {
        return height != null && height >= 100 && height <= 250;
    }

    /**
     * Checks whether the weight value is within a logical range (30-300 kg).
     *
     * @param weight Weight (kg)
     * @return true if valid
     */
    public static boolean isValidWeight(Double weight) {
        return weight != null && weight >= 30 && weight <= 300;
    }

    /**
     * Checks whether the calorie value is logically valid (0-10000).
     *
     * @param calories Calories
     * @return true if valid
     */
    public static boolean isValidCalories(Double calories) {
        return calories != null && calories >= 0 && calories <= 10000;
    }

    /**
     * Checks whether the string length is within a specified range.
     *
     * @param value     Value to check
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @return true if within range
     */
    public static boolean isLengthBetween(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Checks whether the BMI is within a healthy range (18.5-30).
     *
     * @param bmi BMI value
     * @return true if within healthy range
     */
    public static boolean isHealthyBMI(Double bmi) {
        return bmi != null && bmi >= 18.5 && bmi <= 30;
    }

    /**
     * Checks whether the URL is in a valid format.
     *
     * @param url URL
     * @return true if valid
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            new java.net.URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks whether the amount is positive and logically valid.
     *
     * @param amount    Amount
     * @param maxAmount Maximum amount
     * @return true if valid
     */
    public static boolean isValidAmount(Double amount, Double maxAmount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        return maxAmount == null || amount <= maxAmount;
    }

    /**
     * Checks whether two passwords match.
     *
     * @param password        Password
     * @param confirmPassword Confirmation password
     * @return true if they match
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Checks whether the month number is valid (1-12).
     *
     * @param month Month
     * @return true if valid
     */
    public static boolean isValidMonth(Integer month) {
        return month != null && month >= 1 && month <= 12;
    }

    /**
     * Checks whether the day number is valid (1-31).
     *
     * @param day Day
     * @return true if valid
     */
    public static boolean isValidDay(Integer day) {
        return day != null && day >= 1 && day <= 31;
    }

    /**
     * Checks whether the year is valid (2020-2100).
     *
     * @param year Year
     * @return true if valid
     */
    public static boolean isValidYear(Integer year) {
        return year != null && year >= 2020 && year <= 2100;
    }

    /**
     * Creates a validation error message.
     *
     * @param fieldName Field name
     * @param error     Error description
     * @return Error message
     */
    public static String validationError(String fieldName, String error) {
        return String.format("%s: %s", fieldName, error);
    }
}