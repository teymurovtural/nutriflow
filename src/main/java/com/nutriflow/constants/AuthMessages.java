package com.nutriflow.constants;

import lombok.experimental.UtilityClass;

/**
 * Messages used in the Authentication Service
 */
@UtilityClass
public class AuthMessages {

    // ============= REGISTRATION =============
    public static final String REGISTRATION_SUCCESS = "Registration successful. Please verify the code sent to your email.";
    public static final String PASSWORD_MISMATCH = "Passwords do not match!";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists: ";

    // ============= OTP VERIFICATION =============
    public static final String OTP_VERIFIED_SUCCESS = "Your account has been successfully verified!";
    public static final String INVALID_OTP = "Verification code is invalid or has expired.";
    public static final String WRONG_OTP = "Incorrect verification code.";
    public static final String USER_NOT_FOUND = "User not found.";

    // ============= LOGIN =============
    public static final String LOGIN_SUCCESS = "Login successful.";
    public static final String INVALID_CREDENTIALS = "Email or password is incorrect.";
    public static final String LOGIN_FAILED = "An error occurred during login.";
    public static final String SYSTEM_ERROR = "A technical error occurred in the system.";

    // ============= REFRESH TOKEN =============
    public static final String TOKEN_REFRESH_SUCCESS = "Token refresh successful.";
    public static final String INVALID_REFRESH_TOKEN = "Refresh token is invalid!";
    public static final String TOKEN_MISMATCH = "Refresh token mismatch!";

    // ============= ERRORS =============
    public static final String ADMIN_NOT_FOUND = "Admin not found: ";
    public static final String DIETITIAN_NOT_FOUND = "Dietitian not found: ";
    public static final String CATERER_NOT_FOUND = "Caterer not found: ";

    // ============= RESEND OTP =============
    public static final String ALREADY_VERIFIED = "This account is already verified.";
    public static final String OTP_RESENT_SUCCESS = "OTP resent successfully. Please check your email.";

    // ============= FORGOT PASSWORD =============
    public static final String FORGOT_PASSWORD_OTP_SENT = "Password reset code has been sent to your email.";
    public static final String PASSWORD_RESET_SUCCESS = "Your password has been successfully reset. You can now log in.";
    public static final String PASSWORDS_DO_NOT_MATCH = "New passwords do not match.";
}