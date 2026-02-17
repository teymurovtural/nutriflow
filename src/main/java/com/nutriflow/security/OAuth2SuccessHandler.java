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

        // 1. İstifadəçini tapırıq və ya yoxdursa yaradırıq
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .firstName(oauthUser.getAttribute("given_name"))
                            .lastName(oauthUser.getAttribute("family_name"))
                            .email(email)
                            .password("OAUTH2_USER") // Şifrə boş qala bilməz
                            .role(Role.USER)
                            .status(UserStatus.VERIFIED) // Google-dan gəldiyi üçün birbaşa verified
                            .isEmailVerified(true)
                            .phoneNumber("+994000000000") // Müvəqqəti, profil dolduranda yenilənəcək
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

        // 2. Tokenləri yaradırıq
        String accessToken = jwtService.generateToken(securityUser);
        String refreshToken = jwtService.generateRefreshToken(securityUser);

        // 3. Refresh Token-i Redis-ə atırıq
        redisTemplate.opsForValue().set(
                refreshTokenPrefix + securityUser.getUsername(),
                refreshToken,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );

        // 4. Flow Kontrolu
        String targetUrl = determineTargetUrl(user, accessToken, refreshToken);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(UserEntity user, String token, String refreshToken) {
        String baseUrl = "http://localhost:3000";
        String authParams = "?token=" + token + "&refreshToken=" + refreshToken;

        // Mərhələ 1: Sağlamlıq məlumatları yoxdursa
        if (user.getHealthProfile() == null) {
            return baseUrl + "/tell-us-about-yourself" + authParams;
        }

        // Mərhələ 2: Məlumatlar var amma ödəniş edilməyibsə
        if (user.getStatus() == UserStatus.DATA_SUBMITTED) {
            return baseUrl + "/choose-plan" + authParams;
        }

        // Mərhələ 3: Hər şey qaydasındadırsa Dashboard
        if (user.getStatus() == UserStatus.ACTIVE) {
            return baseUrl + "/dashboard" + authParams;
        }

        // Default olaraq ana səhifəyə və ya məlumat doldurmağa
        return baseUrl + "/tell-us-about-yourself" + authParams;
    }
}