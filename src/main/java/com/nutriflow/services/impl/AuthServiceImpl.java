package com.nutriflow.services.impl;

import com.nutriflow.constants.AuthMessages;
import com.nutriflow.dto.request.LoginRequest;
import com.nutriflow.dto.request.RegisterRequest;
import com.nutriflow.dto.request.VerifyRequest;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.Role;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.exceptions.*;
import com.nutriflow.mappers.UserMapper;
import com.nutriflow.repositories.*;
import com.nutriflow.security.JwtService;
import com.nutriflow.security.SecurityUser;
import com.nutriflow.services.AuthService;
import com.nutriflow.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtService jwtService;
    private final OtpRepository otpRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${nutriflow.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Value("${nutriflow.redis.prefix.otp}")
    private String otpPrefix;

    @Value("${nutriflow.redis.prefix.refresh-token}")
    private String refreshTokenPrefix;

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        log.info("Qeydiyyat prosesi başladı: Email = {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Qeydiyyat xətası: Şifrə təsdiqi uğursuzdur - Email: {}", request.getEmail());
            throw new BusinessException(AuthMessages.PASSWORD_MISMATCH);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Qeydiyyat xətası: Email artıq sistemdə var - Email: {}", request.getEmail());
            throw new EmailAlreadyExistsException(AuthMessages.EMAIL_ALREADY_EXISTS + request.getEmail());
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("İstifadəçi bazaya uğurla yazıldı. ID: {}, Email: {}", user.getId(), request.getEmail());

        String otp = generateOtp();
        log.debug("OTP yaradıldı: {}", otp);

        try {
            redisTemplate.opsForValue().set(otpPrefix + request.getEmail(), otp, 5, TimeUnit.MINUTES);
            log.info("OTP Redis-ə yazıldı (5 dəqiqəlik): {}", request.getEmail());
        } catch (Exception e) {
            log.error("Redis-ə yazılarkən xəta baş verdi: {}", e.getMessage());
        }

        OtpEntity otpEntity = OtpEntity.builder()
                .email(request.getEmail())
                .code(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .build();
        otpRepository.save(otpEntity);
        log.info("OTP məlumatı verilənlər bazasında (OtpRepository) saxlanıldı.");

        emailService.sendVerificationEmail(request.getEmail(), otp);
        log.info("Təsdiq emaili göndərildi: {}", request.getEmail());

        return AuthMessages.REGISTRATION_SUCCESS;
    }

    @Override
    @Transactional
    public String verifyOtp(VerifyRequest request) {
        log.info("OTP təsdiqləmə sorğusu gəldi: {}", request.getEmail());

        String storedOtp = redisTemplate.opsForValue().get(otpPrefix + request.getEmail());

        if (storedOtp == null) {
            log.warn("OTP tapılmadı (Müddəti bitmiş ola bilər): {}", request.getEmail());
            throw new InvalidOtpException(AuthMessages.INVALID_OTP);
        }

        if (!storedOtp.equals(request.getOtpCode())) {
            log.warn("Yanlış OTP daxil edildi: Gözlənilən: {}, Daxil edilən: {} - Email: {}",
                    storedOtp, request.getOtpCode(), request.getEmail());
            throw new InvalidOtpException(AuthMessages.WRONG_OTP);
        }

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("OTP təsdiqləmə zamanı email bazada tapılmadı: {}", request.getEmail());
                    return new UserNotFoundException(AuthMessages.USER_NOT_FOUND);
                });

        user.setEmailVerified(true);
        user.setStatus(UserStatus.VERIFIED);
        userRepository.save(user);
        log.info("İstifadəçi statusu VERIFIED olaraq dəyişdirildi: {}", request.getEmail());

        otpRepository.findByEmailAndCode(request.getEmail(), request.getOtpCode())
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    log.info("OTP bazada 'istifadə olunmuş' kimi işarələndi.");
                });

        redisTemplate.delete(otpPrefix + request.getEmail());
        log.info("OTP Redis-dən silindi: {}", request.getEmail());

        return AuthMessages.OTP_VERIFIED_SUCCESS;
    }

    @Override
    public BaseAuthResponse login(LoginRequest request) {
        log.info("Giriş cəhdi başladı: {}", request.getEmail());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            log.info("Authentication Manager tərəfindən doğrulanma uğurlu: {}", request.getEmail());

            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();

            String accessToken = jwtService.generateToken(securityUser);
            String refreshToken = jwtService.generateRefreshToken(securityUser);
            log.info("Access və Refresh tokenlər yaradıldı.");

            redisTemplate.opsForValue().set(
                    refreshTokenPrefix + securityUser.getUsername(),
                    refreshToken,
                    refreshExpiration,
                    TimeUnit.MILLISECONDS
            );
            log.info("Refresh token Redis-də saxlanıldı. İstifadəçi: {}", securityUser.getUsername());

            String role = securityUser.getRole();
            log.info("İstifadəçi rolu: {}", role);

            if (Role.ADMIN.name().equals(role) || Role.SUPER_ADMIN.name().equals(role)) {
                log.info("Admin/SuperAdmin məlumatları gətirilir...");
                AdminEntity a = adminRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException(AuthMessages.ADMIN_NOT_FOUND + request.getEmail()));
                return AdminAuthResponse.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .email(a.getEmail())
                        .firstName(a.getFirstName())
                        .lastName(a.getLastName())
                        .isActive(a.isActive())
                        .role(Role.valueOf(role))
                        .build();
            }

            if (Role.DIETITIAN.name().equals(role)) {
                log.info("Dietoloq məlumatları gətirilir...");
                DietitianEntity d = dietitianRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException(AuthMessages.DIETITIAN_NOT_FOUND + request.getEmail()));
                return DietitianAuthResponse.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .email(d.getEmail())
                        .firstName(d.getFirstName())
                        .lastName(d.getLastName())
                        .specialization(d.getSpecialization())
                        .isActive(d.isActive())
                        .role(Role.DIETITIAN)
                        .build();
            }

            if (Role.CATERER.name().equals(role)) {
                log.info("Caterer məlumatları gətirilir...");
                CatererEntity c = catererRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException(AuthMessages.CATERER_NOT_FOUND + request.getEmail()));
                return CatererAuthResponse.builder()
                        .token(accessToken)
                        .refreshToken(refreshToken)
                        .email(c.getEmail())
                        .companyName(c.getName())
                        .phone(c.getPhone())
                        .status(c.getStatus())
                        .role(Role.CATERER)
                        .build();
            }

            log.info("Standart istifadəçi məlumatları gətirilir...");
            UserEntity u = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(AuthMessages.USER_NOT_FOUND));
            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .email(u.getEmail())
                    .status(u.getStatus())
                    .role(Role.USER)
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Yanlış giriş cəhdi (Email və ya şifrə səhvdir): {}", request.getEmail());
            throw new BusinessException(AuthMessages.INVALID_CREDENTIALS);

        } catch (AuthenticationException e) {
            log.warn("Təhlükəsizlik xətası: {} - {}", request.getEmail(), e.getMessage());
            throw new BusinessException(AuthMessages.LOGIN_FAILED);

        } catch (Exception e) {
            log.error("GÖZLƏNİLMƏZ SİSTEM XƏTASI: {}", e.getMessage());
            throw new BusinessException(AuthMessages.SYSTEM_ERROR);
        }
    }

    @Override
    public BaseAuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token yeniləmə sorğusu gəldi.");

        String cleanToken = (refreshToken != null && refreshToken.startsWith("Bearer "))
                ? refreshToken.substring(7)
                : refreshToken;

        String userEmail = jwtService.extractUsername(cleanToken);
        log.info("Token daxilindən email oxundu: {}", userEmail);

        String storedToken = redisTemplate.opsForValue().get(refreshTokenPrefix + userEmail);

        if (storedToken == null) {
            log.warn("Refresh token Redis-də tapılmadı: {}", userEmail);
            throw new InvalidTokenException(AuthMessages.INVALID_REFRESH_TOKEN);
        }

        if (!storedToken.equals(cleanToken)) {
            log.error("Təqdim edilən refresh token Redis-dəki ilə uyğun deyil: {}", userEmail);
            throw new InvalidTokenException(AuthMessages.TOKEN_MISMATCH);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        String newAccessToken = jwtService.generateToken(userDetails);
        SecurityUser securityUser = (SecurityUser) userDetails;
        String role = securityUser.getRole();

        log.info("Yeni Access Token yaradıldı. Rol: {}", role);

        if (Role.ADMIN.name().equals(role) || Role.SUPER_ADMIN.name().equals(role)) {
            AdminEntity a = adminRepository.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("Refresh zamanı Admin tapılmadı: {}", userEmail);
                        return new UserNotFoundException(AuthMessages.ADMIN_NOT_FOUND);
                    });
            return AdminAuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(cleanToken)
                    .email(a.getEmail())
                    .firstName(a.getFirstName())
                    .lastName(a.getLastName())
                    .isActive(a.isActive())
                    .role(Role.valueOf(role))
                    .build();
        }

        if (Role.DIETITIAN.name().equals(role)) {
            DietitianEntity d = dietitianRepository.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("Refresh zamanı Dietoloq tapılmadı: {}", userEmail);
                        return new UserNotFoundException(AuthMessages.DIETITIAN_NOT_FOUND);
                    });
            return DietitianAuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(cleanToken)
                    .email(d.getEmail())
                    .firstName(d.getFirstName())
                    .lastName(d.getLastName())
                    .specialization(d.getSpecialization())
                    .isActive(d.isActive())
                    .role(Role.DIETITIAN)
                    .build();
        }

        if (Role.CATERER.name().equals(role)) {
            CatererEntity c = catererRepository.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("Refresh zamanı Caterer tapılmadı: {}", userEmail);
                        return new UserNotFoundException(AuthMessages.CATERER_NOT_FOUND);
                    });
            return CatererAuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(cleanToken)
                    .email(c.getEmail())
                    .companyName(c.getName())
                    .phone(c.getPhone())
                    .status(c.getStatus())
                    .role(Role.CATERER)
                    .build();
        }

        UserEntity u = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Refresh zamanı istifadəçi tapılmadı: {}", userEmail);
                    return new UserNotFoundException(AuthMessages.USER_NOT_FOUND);
                });

        log.info("Token yeniləmə uğurla tamamlandı: {}", userEmail);
        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(cleanToken)
                .email(u.getEmail())
                .status(u.getStatus())
                .role(Role.USER)
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}