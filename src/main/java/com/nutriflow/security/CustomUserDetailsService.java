package com.nutriflow.security;

import com.nutriflow.entities.AdminEntity;
import com.nutriflow.entities.CatererEntity;
import com.nutriflow.entities.DietitianEntity;
import com.nutriflow.enums.CatererStatus;
import com.nutriflow.enums.Role;
import com.nutriflow.repositories.AdminRepository;
import com.nutriflow.repositories.CatererRepository;
import com.nutriflow.repositories.DietitianRepository;
import com.nutriflow.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Loading user details (loadUserByUsername): {}", email);

        // 1. Admin check
        var admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            log.info("Email found in Admin table: {}", email);
            AdminEntity a = admin.get();
            if (!a.isActive()) {
                log.warn("Access denied: Admin is inactive - {}", email);
                throw new UsernameNotFoundException("Admin has been deactivated: " + email);
            }
            String adminRole = a.isSuperAdmin() ? Role.SUPER_ADMIN.name() : Role.ADMIN.name();
            return new SecurityUser(a.getId(), a.getEmail(), a.getPassword(),
                    adminRole, true);
        }

        // 2. Dietitian check
        var diet = dietitianRepository.findByEmail(email);
        if (diet.isPresent()) {
            log.info("Email found in Dietitian table: {}", email);
            DietitianEntity d = diet.get();
            if (!d.isActive()) {
                log.warn("Access denied: Dietitian is inactive - {}", email);
                throw new UsernameNotFoundException("Dietitian has been deactivated: " + email);
            }
            return new SecurityUser(d.getId(), d.getEmail(), d.getPassword(),
                    d.getRole().name(), true);
        }

        // 3. Caterer check
        var caterer = catererRepository.findByEmail(email);
        if (caterer.isPresent()) {
            log.info("Email found in Caterer table: {}", email);
            CatererEntity c = caterer.get();
            boolean isActive = c.getStatus() != null && c.getStatus().equals(CatererStatus.ACTIVE);
            if (!isActive) {
                log.warn("Access denied: Caterer is not active - Status: {}, Email: {}", c.getStatus(), email);
                throw new UsernameNotFoundException("Caterer has been deactivated: " + email);
            }
            return new SecurityUser(c.getId(), c.getEmail(), c.getPassword(),
                    c.getRole().name(), true);
        }

        // 4. User check
        log.debug("Not found in other roles, checking User table: {}", email);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Login failed: Email not found in any table - {}", email);
                    return new UsernameNotFoundException("Email not found in the system: " + email);
                });

        if (!user.isEmailVerified()) {
            log.warn("Access denied: User's email is not verified - {}", email);
            throw new UsernameNotFoundException("User has been deactivated: " + email);
        }

        log.info("User found successfully and SecurityUser object created: {}", email);
        return new SecurityUser(user.getId(), user.getEmail(), user.getPassword(),
                user.getRole().name(), true);
    }
}