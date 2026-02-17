package com.nutriflow.services;

import com.nutriflow.dto.request.LoginRequest;
import com.nutriflow.dto.request.RegisterRequest;
import com.nutriflow.dto.request.VerifyRequest;
import com.nutriflow.dto.response.BaseAuthResponse;
import com.nutriflow.dto.response.TokenResponse; // Yeni DTO lazımdırsa yaradılmalıdır

public interface AuthService {
    String register(RegisterRequest request);
    String verifyOtp(VerifyRequest request);
    BaseAuthResponse login(LoginRequest request);
    BaseAuthResponse refreshToken(String refreshToken); // Refresh məntiqi üçün
}