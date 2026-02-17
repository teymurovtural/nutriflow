package com.nutriflow.constants;

import lombok.experimental.UtilityClass;

/**
 * Authentication Service-də istifadə olunan mesajlar
 */
@UtilityClass
public class AuthMessages {

    // ============= REGISTRATION =============
    public static final String REGISTRATION_SUCCESS = "Qeydiyyat uğurludur. Emailinizə göndərilən kodu təsdiqləyin.";
    public static final String PASSWORD_MISMATCH = "Şifrələr uyğun gəlmir!";
    public static final String EMAIL_ALREADY_EXISTS = "Email artıq mövcuddur: ";

    // ============= OTP VERIFICATION =============
    public static final String OTP_VERIFIED_SUCCESS = "Hesabınız uğurla təsdiqləndi!";
    public static final String INVALID_OTP = "Təsdiq kodu yanlışdır və ya vaxtı bitib.";
    public static final String WRONG_OTP = "Təsdiq kodu yanlışdır.";
    public static final String USER_NOT_FOUND = "İstifadəçi tapılmadı.";

    // ============= LOGIN =============
    public static final String LOGIN_SUCCESS = "Giriş uğurlu.";
    public static final String INVALID_CREDENTIALS = "Email və ya şifrə yanlışdır.";
    public static final String LOGIN_FAILED = "Giriş zamanı xəta baş verdi.";
    public static final String SYSTEM_ERROR = "Sistemdə texniki xəta baş verdi.";

    // ============= REFRESH TOKEN =============
    public static final String TOKEN_REFRESH_SUCCESS = "Token yeniləmə uğurlu.";
    public static final String INVALID_REFRESH_TOKEN = "Refresh token etibarsızdır!";
    public static final String TOKEN_MISMATCH = "Refresh token uyğunsuzluğu!";

    // ============= ERRORS =============
    public static final String ADMIN_NOT_FOUND = "Admin tapılmadı: ";
    public static final String DIETITIAN_NOT_FOUND = "Dietoloq tapılmadı: ";
    public static final String CATERER_NOT_FOUND = "Caterer tapılmadı: ";
}