package com.nutriflow.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Validation əməliyyatları üçün yardımçı sinif.
 * Email, telefon, şifrə və digər məlumatların yoxlanılması.
 */
@Slf4j
public class ValidationUtils {

    // Regex pattern-lər
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
     * Email-in düzgün formatda olub-olmadığını yoxlayır.
     *
     * @param email Email ünvanı
     * @return true əgər düzgündürsə
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Telefon nömrəsinin Azərbaycan formatında olub-olmadığını yoxlayır.
     * Format: +994XXXXXXXXX və ya 0XXXXXXXXX
     *
     * @param phone Telefon nömrəsi
     * @return true əgər düzgündürsə
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("\\s", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    /**
     * Şifrənin güclü olub-olmadığını yoxlayır.
     * Tələblər:
     * - Minimum 8 simvol
     * - Ən azı 1 rəqəm
     * - Ən azı 1 kiçik hərf
     * - Ən azı 1 böyük hərf
     * - Ən azı 1 xüsusi simvol (@#$%^&+=!)
     *
     * @param password Şifrə
     * @return true əgər güclüdürsə
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * String-in boş olub-olmadığını yoxlayır (null və ya blank).
     *
     * @param value Yoxlanılacaq dəyər
     * @return true əgər boşdursa
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }

    /**
     * String-in boş olmadığını yoxlayır.
     *
     * @param value Yoxlanılacaq dəyər
     * @return true əgər boş deyilsə
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * Rəqəmin müsbət olub-olmadığını yoxlayır.
     *
     * @param number Yoxlanılacaq rəqəm
     * @return true əgər müsbətdirsə
     */
    public static boolean isPositive(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }

    /**
     * Rəqəmin qeyri-mənfi olub-olmadığını yoxlayır (0 və ya böyük).
     *
     * @param number Yoxlanılacaq rəqəm
     * @return true əgər qeyri-mənfidirsə
     */
    public static boolean isNonNegative(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() >= 0;
    }

    /**
     * Boy dəyərinin məntiqli aralıqda olub-olmadığını yoxlayır (100-250 cm).
     *
     * @param height Boy (sm)
     * @return true əgər düzgündürsə
     */
    public static boolean isValidHeight(Double height) {
        return height != null && height >= 100 && height <= 250;
    }

    /**
     * Çəki dəyərinin məntiqli aralıqda olub-olmadığını yoxlayır (30-300 kg).
     *
     * @param weight Çəki (kq)
     * @return true əgər düzgündürsə
     */
    public static boolean isValidWeight(Double weight) {
        return weight != null && weight >= 30 && weight <= 300;
    }

    /**
     * Kalori dəyərinin məntiqli olub-olmadığını yoxlayır (0-10000).
     *
     * @param calories Kalori
     * @return true əgər düzgündürsə
     */
    public static boolean isValidCalories(Double calories) {
        return calories != null && calories >= 0 && calories <= 10000;
    }

    /**
     * String-in müəyyən uzunluq aralığında olub-olmadığını yoxlayır.
     *
     * @param value     Yoxlanılacaq dəyər
     * @param minLength Minimum uzunluq
     * @param maxLength Maximum uzunluq
     * @return true əgər aralıqdadırsa
     */
    public static boolean isLengthBetween(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * BMI-nin sağlam aralıqda olub-olmadığını yoxlayır (18.5-30).
     *
     * @param bmi BMI dəyəri
     * @return true əgər sağlam aralıqdadırsa
     */
    public static boolean isHealthyBMI(Double bmi) {
        return bmi != null && bmi >= 18.5 && bmi <= 30;
    }

    /**
     * URL-in düzgün formatda olub-olmadığını yoxlayır.
     *
     * @param url URL
     * @return true əgər düzgündürsə
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
     * Məbləğin müsbət və məntiqli olub-olmadığını yoxlayır.
     *
     * @param amount    Məbləğ
     * @param maxAmount Maximum məbləğ
     * @return true əgər düzgündürsə
     */
    public static boolean isValidAmount(Double amount, Double maxAmount) {
        if (amount == null || amount <= 0) {
            return false;
        }
        return maxAmount == null || amount <= maxAmount;
    }

    /**
     * İki şifrənin uyğun olub-olmadığını yoxlayır.
     *
     * @param password        Şifrə
     * @param confirmPassword Təsdiq şifrəsi
     * @return true əgər uyğundursa
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    /**
     * Ay rəqəminin düzgün olub-olmadığını yoxlayır (1-12).
     *
     * @param month Ay
     * @return true əgər düzgündürsə
     */
    public static boolean isValidMonth(Integer month) {
        return month != null && month >= 1 && month <= 12;
    }

    /**
     * Günün düzgün olub-olmadığını yoxlayır (1-31).
     *
     * @param day Gün
     * @return true əgər düzgündürsə
     */
    public static boolean isValidDay(Integer day) {
        return day != null && day >= 1 && day <= 31;
    }

    /**
     * İlin düzgün olub-olmadığını yoxlayır (2020-2100).
     *
     * @param year İl
     * @return true əgər düzgündürsə
     */
    public static boolean isValidYear(Integer year) {
        return year != null && year >= 2020 && year <= 2100;
    }

    /**
     * Validation xəta mesajı yaradır.
     *
     * @param fieldName Sahə adı
     * @param error     Xəta
     * @return Xəta mesajı
     */
    public static String validationError(String fieldName, String error) {
        return String.format("%s: %s", fieldName, error);
    }
}