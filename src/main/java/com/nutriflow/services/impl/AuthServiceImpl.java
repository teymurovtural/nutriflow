package com.nutriflow.services.impl;

import com.nutriflow.constants.AuthMessages;
import com.nutriflow.dto.request.*;
import com.nutriflow.dto.response.*;
import com.nutriflow.entities.*;
import com.nutriflow.enums.Role;
import com.nutriflow.enums.UserStatus;
import com.nutriflow.exceptions.*;
import com.nutriflow.helpers.OtpHelper;
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
    private final OtpHelper otpHelper;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${nutriflow.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Value("${nutriflow.redis.prefix.otp}")
    private String otpPrefix;

    @Value("${nutriflow.redis.prefix.refresh-token}")
    private String refreshTokenPrefix;

    // ===================== REGISTER =====================

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        log.info("Registration process started: Email = {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(AuthMessages.PASSWORD_MISMATCH);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(AuthMessages.EMAIL_ALREADY_EXISTS + request.getEmail());
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("User saved: ID={}, Email={}", user.getId(), request.getEmail());

        // ✅ OtpHelper istifadə et
        String otp = otpHelper.createAndSaveOtp(request.getEmail(), otpPrefix);

        emailService.sendVerificationEmail(request.getEmail(), otp);
        log.info("Verification email sent: {}", request.getEmail());

        return AuthMessages.REGISTRATION_SUCCESS;
    }

    // ===================== VERIFY OTP =====================

    @Override
    @Transactional
    public String verifyOtp(VerifyRequest request) {
        log.info("OTP verification request: {}", request.getEmail());

        // ✅ OtpHelper istifadə et
        otpHelper.validateOtp(request.getEmail(), request.getOtpCode(), otpPrefix);

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(AuthMessages.USER_NOT_FOUND));

        user.setEmailVerified(true);
        user.setStatus(UserStatus.VERIFIED);
        userRepository.save(user);
        log.info("User status changed to VERIFIED: {}", request.getEmail());

        // ✅ OtpHelper istifadə et
        otpHelper.markOtpAsUsed(request.getEmail(), request.getOtpCode(), otpPrefix);

        return AuthMessages.OTP_VERIFIED_SUCCESS;
    }

    // ===================== LOGIN =====================

    @Override
    public BaseAuthResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();
            String accessToken = jwtService.generateToken(securityUser);
            String refreshToken = jwtService.generateRefreshToken(securityUser);

            redisTemplate.opsForValue().set(
                    refreshTokenPrefix + securityUser.getUsername(),
                    refreshToken, refreshExpiration, TimeUnit.MILLISECONDS
            );

            String role = securityUser.getRole();

            if (Role.ADMIN.name().equals(role) || Role.SUPER_ADMIN.name().equals(role)) {
                AdminEntity a = adminRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException(AuthMessages.ADMIN_NOT_FOUND + request.getEmail()));
                return AdminAuthResponse.builder()
                        .token(accessToken).refreshToken(refreshToken)
                        .email(a.getEmail()).firstName(a.getFirstName()).lastName(a.getLastName())
                        .isActive(a.isActive()).role(Role.valueOf(role)).build();
            }

            if (Role.DIETITIAN.name().equals(role)) {
                DietitianEntity d = dietitianRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException(AuthMessages.DIETITIAN_NOT_FOUND + request.getEmail()));
                return DietitianAuthResponse.builder()
                        .token(accessToken).refreshToken(refreshToken)
                        .email(d.getEmail()).firstName(d.getFirstName()).lastName(d.getLastName())
                        .specialization(d.getSpecialization()).isActive(d.isActive()).role(Role.DIETITIAN).build();
            }

            if (Role.CATERER.name().equals(role)) {
                CatererEntity c = catererRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new UserNotFoundException(AuthMessages.CATERER_NOT_FOUND + request.getEmail()));
                return CatererAuthResponse.builder()
                        .token(accessToken).refreshToken(refreshToken)
                        .email(c.getEmail()).companyName(c.getName()).phone(c.getPhone())
                        .status(c.getStatus()).role(Role.CATERER).build();
            }

            UserEntity u = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UserNotFoundException(AuthMessages.USER_NOT_FOUND));
            return AuthResponse.builder()
                    .token(accessToken).refreshToken(refreshToken)
                    .email(u.getEmail()).status(u.getStatus()).role(Role.USER).build();

        } catch (BadCredentialsException e) {
            throw new BusinessException(AuthMessages.INVALID_CREDENTIALS);
        } catch (AuthenticationException e) {
            throw new BusinessException(AuthMessages.LOGIN_FAILED);
        } catch (Exception e) {
            log.error("Unexpected error during login: {}", e.getMessage());
            throw new BusinessException(AuthMessages.SYSTEM_ERROR);
        }
    }

    // ===================== REFRESH TOKEN =====================

    @Override
    public BaseAuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token request received.");

        String cleanToken = (refreshToken != null && refreshToken.startsWith("Bearer "))
                ? refreshToken.substring(7) : refreshToken;

        String userEmail = jwtService.extractUsername(cleanToken);
        String storedToken = redisTemplate.opsForValue().get(refreshTokenPrefix + userEmail);

        if (storedToken == null) {
            throw new InvalidTokenException(AuthMessages.INVALID_REFRESH_TOKEN);
        }
        if (!storedToken.equals(cleanToken)) {
            throw new InvalidTokenException(AuthMessages.TOKEN_MISMATCH);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        String newAccessToken = jwtService.generateToken(userDetails);
        SecurityUser securityUser = (SecurityUser) userDetails;
        String role = securityUser.getRole();

        if (Role.ADMIN.name().equals(role) || Role.SUPER_ADMIN.name().equals(role)) {
            AdminEntity a = adminRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException(AuthMessages.ADMIN_NOT_FOUND));
            return AdminAuthResponse.builder()
                    .token(newAccessToken).refreshToken(cleanToken)
                    .email(a.getEmail()).firstName(a.getFirstName()).lastName(a.getLastName())
                    .isActive(a.isActive()).role(Role.valueOf(role)).build();
        }

        if (Role.DIETITIAN.name().equals(role)) {
            DietitianEntity d = dietitianRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException(AuthMessages.DIETITIAN_NOT_FOUND));
            return DietitianAuthResponse.builder()
                    .token(newAccessToken).refreshToken(cleanToken)
                    .email(d.getEmail()).firstName(d.getFirstName()).lastName(d.getLastName())
                    .specialization(d.getSpecialization()).isActive(d.isActive()).role(Role.DIETITIAN).build();
        }

        if (Role.CATERER.name().equals(role)) {
            CatererEntity c = catererRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException(AuthMessages.CATERER_NOT_FOUND));
            return CatererAuthResponse.builder()
                    .token(newAccessToken).refreshToken(cleanToken)
                    .email(c.getEmail()).companyName(c.getName()).phone(c.getPhone())
                    .status(c.getStatus()).role(Role.CATERER).build();
        }

        UserEntity u = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException(AuthMessages.USER_NOT_FOUND));
        return AuthResponse.builder()
                .token(newAccessToken).refreshToken(cleanToken)
                .email(u.getEmail()).status(u.getStatus()).role(Role.USER).build();
    }

    // ===================== RESEND OTP =====================

    @Override
    @Transactional
    public String resendOtp(String email) {
        log.info("Resend OTP request: {}", email);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(AuthMessages.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            throw new BusinessException(AuthMessages.ALREADY_VERIFIED);
        }

        // ✅ OtpHelper istifadə et
        String newOtp = otpHelper.createAndSaveOtp(email, otpPrefix);

        emailService.sendVerificationEmail(email, newOtp);
        log.info("New OTP email sent: {}", email);

        return AuthMessages.OTP_RESENT_SUCCESS;
    }

    // ===================== FORGOT PASSWORD =====================

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request: {}", request.getEmail());

        boolean exists = userRepository.existsByEmail(request.getEmail())
                || dietitianRepository.existsByEmail(request.getEmail())
                || catererRepository.existsByEmail(request.getEmail())
                || adminRepository.existsByEmail(request.getEmail());

        if (!exists) {
            log.warn("Email not found in any table: {}", request.getEmail());
            throw new UserNotFoundException(AuthMessages.USER_NOT_FOUND);
        }

        String otp = otpHelper.createAndSaveOtp(request.getEmail(), otpPrefix);
        emailService.sendForgotPasswordEmail(request.getEmail(), otp);
        log.info("Forgot password OTP sent: {}", request.getEmail());

        return AuthMessages.FORGOT_PASSWORD_OTP_SENT;
    }

    // ===================== RESET PASSWORD =====================

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        log.info("Reset password request: {}", request.getEmail());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(AuthMessages.PASSWORDS_DO_NOT_MATCH);
        }

        otpHelper.validateOtp(request.getEmail(), request.getOtp(), otpPrefix);

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            UserEntity user = userRepository.findByEmail(email).get();
            user.setPassword(encodedPassword);
            userRepository.save(user);
            log.info("Password updated in users table: {}", email);

        } else if (dietitianRepository.existsByEmail(email)) {
            DietitianEntity dietitian = dietitianRepository.findByEmail(email).get();
            dietitian.setPassword(encodedPassword);
            dietitianRepository.save(dietitian);
            log.info("Password updated in dietitians table: {}", email);

        } else if (catererRepository.existsByEmail(email)) {
            CatererEntity caterer = catererRepository.findByEmail(email).get();
            caterer.setPassword(encodedPassword);
            catererRepository.save(caterer);
            log.info("Password updated in caterers table: {}", email);

        } else if (adminRepository.existsByEmail(email)) {
            AdminEntity admin = adminRepository.findByEmail(email).get();
            admin.setPassword(encodedPassword);
            adminRepository.save(admin);
            log.info("Password updated in admins table: {}", email);

        } else {
            throw new UserNotFoundException(AuthMessages.USER_NOT_FOUND);
        }

        otpHelper.markOtpAsUsed(request.getEmail(), request.getOtp(), otpPrefix);

        return AuthMessages.PASSWORD_RESET_SUCCESS;
    }
}