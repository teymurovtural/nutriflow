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
        log.info("Registration process started: Email = {}", request.getEmail());

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Registration error: Password confirmation failed - Email: {}", request.getEmail());
            throw new BusinessException(AuthMessages.PASSWORD_MISMATCH);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration error: Email already exists in the system - Email: {}", request.getEmail());
            throw new EmailAlreadyExistsException(AuthMessages.EMAIL_ALREADY_EXISTS + request.getEmail());
        }

        UserEntity user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("User successfully saved to database. ID: {}, Email: {}", user.getId(), request.getEmail());

        String otp = generateOtp();
        log.debug("OTP generated: {}", otp);

        try {
            redisTemplate.opsForValue().set(otpPrefix + request.getEmail(), otp, 5, TimeUnit.MINUTES);
            log.info("OTP written to Redis (5-minute TTL): {}", request.getEmail());
        } catch (Exception e) {
            log.error("Error occurred while writing to Redis: {}", e.getMessage());
        }

        OtpEntity otpEntity = OtpEntity.builder()
                .email(request.getEmail())
                .code(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .build();
        otpRepository.save(otpEntity);
        log.info("OTP data saved to the database (OtpRepository).");

        emailService.sendVerificationEmail(request.getEmail(), otp);
        log.info("Verification email sent: {}", request.getEmail());

        return AuthMessages.REGISTRATION_SUCCESS;
    }

    @Override
    @Transactional
    public String verifyOtp(VerifyRequest request) {
        log.info("OTP verification request received: {}", request.getEmail());

        String storedOtp = redisTemplate.opsForValue().get(otpPrefix + request.getEmail());

        if (storedOtp == null) {
            log.warn("OTP not found (may have expired): {}", request.getEmail());
            throw new InvalidOtpException(AuthMessages.INVALID_OTP);
        }

        if (!storedOtp.equals(request.getOtpCode())) {
            log.warn("Incorrect OTP entered - Expected: {}, Entered: {} - Email: {}",
                    storedOtp, request.getOtpCode(), request.getEmail());
            throw new InvalidOtpException(AuthMessages.WRONG_OTP);
        }

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Email not found in database during OTP verification: {}", request.getEmail());
                    return new UserNotFoundException(AuthMessages.USER_NOT_FOUND);
                });

        user.setEmailVerified(true);
        user.setStatus(UserStatus.VERIFIED);
        userRepository.save(user);
        log.info("User status changed to VERIFIED: {}", request.getEmail());

        otpRepository.findByEmailAndCode(request.getEmail(), request.getOtpCode())
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                    log.info("OTP marked as 'used' in the database.");
                });

        redisTemplate.delete(otpPrefix + request.getEmail());
        log.info("OTP deleted from Redis: {}", request.getEmail());

        return AuthMessages.OTP_VERIFIED_SUCCESS;
    }

    @Override
    public BaseAuthResponse login(LoginRequest request) {
        log.info("Login attempt started: {}", request.getEmail());

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            log.info("Authentication successful via Authentication Manager: {}", request.getEmail());

            SecurityUser securityUser = (SecurityUser) auth.getPrincipal();

            String accessToken = jwtService.generateToken(securityUser);
            String refreshToken = jwtService.generateRefreshToken(securityUser);
            log.info("Access and Refresh tokens generated.");

            redisTemplate.opsForValue().set(
                    refreshTokenPrefix + securityUser.getUsername(),
                    refreshToken,
                    refreshExpiration,
                    TimeUnit.MILLISECONDS
            );
            log.info("Refresh token stored in Redis. User: {}", securityUser.getUsername());

            String role = securityUser.getRole();
            log.info("User role: {}", role);

            if (Role.ADMIN.name().equals(role) || Role.SUPER_ADMIN.name().equals(role)) {
                log.info("Fetching Admin/SuperAdmin data...");
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
                log.info("Fetching Dietitian data...");
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
                log.info("Fetching Caterer data...");
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

            log.info("Fetching standard user data...");
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
            log.warn("Invalid login attempt (incorrect email or password): {}", request.getEmail());
            throw new BusinessException(AuthMessages.INVALID_CREDENTIALS);

        } catch (AuthenticationException e) {
            log.warn("Security error: {} - {}", request.getEmail(), e.getMessage());
            throw new BusinessException(AuthMessages.LOGIN_FAILED);

        } catch (Exception e) {
            log.error("UNEXPECTED SYSTEM ERROR: {}", e.getMessage());
            throw new BusinessException(AuthMessages.SYSTEM_ERROR);
        }
    }

    @Override
    public BaseAuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token renewal request received.");

        String cleanToken = (refreshToken != null && refreshToken.startsWith("Bearer "))
                ? refreshToken.substring(7)
                : refreshToken;

        String userEmail = jwtService.extractUsername(cleanToken);
        log.info("Email extracted from token: {}", userEmail);

        String storedToken = redisTemplate.opsForValue().get(refreshTokenPrefix + userEmail);

        if (storedToken == null) {
            log.warn("Refresh token not found in Redis: {}", userEmail);
            throw new InvalidTokenException(AuthMessages.INVALID_REFRESH_TOKEN);
        }

        if (!storedToken.equals(cleanToken)) {
            log.error("Provided refresh token does not match the one stored in Redis: {}", userEmail);
            throw new InvalidTokenException(AuthMessages.TOKEN_MISMATCH);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
        String newAccessToken = jwtService.generateToken(userDetails);
        SecurityUser securityUser = (SecurityUser) userDetails;
        String role = securityUser.getRole();

        log.info("New Access Token generated. Role: {}", role);

        if (Role.ADMIN.name().equals(role) || Role.SUPER_ADMIN.name().equals(role)) {
            AdminEntity a = adminRepository.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        log.error("Admin not found during refresh: {}", userEmail);
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
                        log.error("Dietitian not found during refresh: {}", userEmail);
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
                        log.error("Caterer not found during refresh: {}", userEmail);
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
                    log.error("User not found during refresh: {}", userEmail);
                    return new UserNotFoundException(AuthMessages.USER_NOT_FOUND);
                });

        log.info("Token renewal completed successfully: {}", userEmail);
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