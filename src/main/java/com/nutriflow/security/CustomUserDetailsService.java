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
        log.info("İstifadəçi detalları yüklənir (loadUserByUsername): {}", email);

        // 1. Admin yoxlanışı
        var admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            log.info("Email Admin cədvəlində tapıldı: {}", email);
            AdminEntity a = admin.get();
            if (!a.isActive()) {
                log.warn("Giriş rədd edildi: Admin deaktivdir - {}", email);
                throw new UsernameNotFoundException("Admin deaktiv edilmişdir: " + email);
            }
            String adminRole = a.isSuperAdmin() ? Role.SUPER_ADMIN.name() : Role.ADMIN.name();
            return new SecurityUser(a.getId(), a.getEmail(), a.getPassword(),
                    adminRole, true);
        }

        // 2. Dietitian yoxlanışı
        var diet = dietitianRepository.findByEmail(email);
        if (diet.isPresent()) {
            log.info("Email Dietitian cədvəlində tapıldı: {}", email);
            DietitianEntity d = diet.get();
            if (!d.isActive()) {
                log.warn("Giriş rədd edildi: Dietoloq deaktivdir - {}", email);
                throw new UsernameNotFoundException("Dietitian deaktiv edilmişdir: " + email);
            }
            return new SecurityUser(d.getId(), d.getEmail(), d.getPassword(),
                    d.getRole().name(), true);
        }

        // 3. Caterer yoxlanışı
        var caterer = catererRepository.findByEmail(email);
        if (caterer.isPresent()) {
            log.info("Email Caterer cədvəlində tapıldı: {}", email);
            CatererEntity c = caterer.get();
            boolean isActive = c.getStatus() != null && c.getStatus().equals(CatererStatus.ACTIVE);
            if (!isActive) {
                log.warn("Giriş rədd edildi: Caterer aktiv deyil - Status: {}, Email: {}", c.getStatus(), email);
                throw new UsernameNotFoundException("Caterer deaktiv edilmişdir: " + email);
            }
            return new SecurityUser(c.getId(), c.getEmail(), c.getPassword(),
                    c.getRole().name(), true);
        }

        // 4. User yoxlanışı
        log.debug("Digər rollarda tapılmadı, User cədvəli yoxlanılır: {}", email);
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Giriş uğursuz: Email heç bir cədvəldə tapılmadı - {}", email);
                    return new UsernameNotFoundException("Sistem-də bu email tapılmadı: " + email);
                });

        if (!user.isEmailVerified()) {
            log.warn("Giriş rədd edildi: İstifadəçinin emaili təsdiqlənməyib - {}", email);
            throw new UsernameNotFoundException("User deaktiv edilmişdir: " + email);
        }

        log.info("İstifadəçi uğurla tapıldı və SecurityUser obyekti yaradıldı: {}", email);
        return new SecurityUser(user.getId(), user.getEmail(), user.getPassword(),
                user.getRole().name(), true);
    }
}