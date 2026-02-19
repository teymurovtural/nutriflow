package com.nutriflow.services;

import com.nutriflow.dto.request.*;
import com.nutriflow.dto.response.BaseAuthResponse;
import com.nutriflow.dto.response.TokenResponse; // Should be created if new DTO is needed

public interface AuthService {
    String register(RegisterRequest request);
    String verifyOtp(VerifyRequest request);
    BaseAuthResponse login(LoginRequest request);
    BaseAuthResponse refreshToken(String refreshToken); // For refresh logic
    String resendOtp(String email);
    String forgotPassword(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
}