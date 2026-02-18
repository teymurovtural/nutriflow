package com.nutriflow.controllers;

import com.nutriflow.dto.request.LoginRequest;
import com.nutriflow.dto.request.RegisterRequest;
import com.nutriflow.dto.request.VerifyRequest;
import com.nutriflow.dto.response.BaseAuthResponse;
import com.nutriflow.services.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/google-login-url")
    public ResponseEntity<String> getGoogleLoginUrl() {
        return ResponseEntity.ok("http://localhost:8080/oauth2/authorization/google");
    }

    /**
     * Refresh Token endpoint.
     * Validates that the token coming via @RequestHeader is not blank.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<BaseAuthResponse> refreshToken(
            @RequestHeader("Authorization") @NotBlank(message = "Refresh token cannot be blank") String authHeader
    ) {
        // Passing the header directly to the Service; it will be cleaned up inside
        return ResponseEntity.ok(authService.refreshToken(authHeader));
    }
}