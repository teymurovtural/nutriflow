package com.nutriflow.security;

import com.nutriflow.entities.UserEntity;
import com.nutriflow.enums.Role;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${nutriflow.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Value("${nutriflow.redis.prefix.refresh-token}")
    private String refreshTokenPrefix;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        // 1. Find the user or create one if not found
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .firstName(oauthUser.getAttribute("given_name"))
                            .lastName(oauthUser.getAttribute("family_name"))
                            .email(email)
                            .password("OAUTH2_USER") // Password cannot be empty
                            .role(Role.USER)
                            .status(UserStatus.VERIFIED) // Directly verified since coming from Google
                            .isEmailVerified(true)
                            .phoneNumber("+994000000000") // Temporary, will be updated when profile is filled
                            .build();
                    return userRepository.save(newUser);
                });

        SecurityUser securityUser = new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.isEmailVerified()
        );

        // 2. Generate tokens
        String accessToken = jwtService.generateToken(securityUser);
        String refreshToken = jwtService.generateRefreshToken(securityUser);

        // 3. Store Refresh Token in Redis
        redisTemplate.opsForValue().set(
                refreshTokenPrefix + securityUser.getUsername(),
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        // 4. Flow Control
        String targetUrl = determineTargetUrl(user, accessToken, refreshToken);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(UserEntity user, String token, String refreshToken) {
        String baseUrl = "http://localhost:3000";
        String authParams = "?token=" + token + "&refreshToken=" + refreshToken;

        // Step 1: If health data is missing
        if (user.getHealthProfile() == null) {
            return baseUrl + "/tell-us-about-yourself" + authParams;
        }

        // Step 2: Data exists but payment has not been made
        if (user.getStatus() == UserStatus.DATA_SUBMITTED) {
            return baseUrl + "/choose-plan" + authParams;
        }

        // Step 3: Everything is in order - go to Dashboard
        if (user.getStatus() == UserStatus.ACTIVE) {
            return baseUrl + "/dashboard" + authParams;
        }

        // Default - go to home page or data entry
        return baseUrl + "/tell-us-about-yourself" + authParams;
    }
}