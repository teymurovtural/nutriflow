package com.nutriflow.config;

import com.nutriflow.entities.*;
import com.nutriflow.enums.*;
import com.nutriflow.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository; // AdminEntity üçün
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 1. ADMIN YARATMAQ (AdminEntity cədvəlinə)
        if (adminRepository.findByEmail("admin@nutriflow.com").isEmpty()) {
            AdminEntity admin = AdminEntity.builder()
                    .firstName("Tural")
                    .lastName("Teymurov")
                    .email("admin@nutriflow.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .isSuperAdmin(true)
                    .isActive(true)
                    .build();
            adminRepository.save(admin);
            System.out.println(">>> Super Admin Yaradıldı: admin@nutriflow.com");
        }

        // 2. DIETOLOQ YARATMAQ (Dietitians cədvəlinə)
        if (dietitianRepository.findByEmail("diet@nutriflow.com").isEmpty()) {
            DietitianEntity dietitian = DietitianEntity.builder()
                    .firstName("Leyla")
                    .lastName("Aliyeva")
                    .email("diet@nutriflow.com")
                    .password(passwordEncoder.encode("diet123"))
                    .phone("+994501111111")
                    .specialization("Fərdi Qidalanma Mütəxəssisi")
                    .isActive(true)
                    .build();
            dietitianRepository.save(dietitian);
            System.out.println(">>> Default Dietoloq Yaradıldı: diet@nutriflow.com");
        }

        // 3. CATERER YARATMAQ (Caterers cədvəlinə)
        if (catererRepository.findByEmail("caterer@nutriflow.com").isEmpty()) {
            CatererEntity caterer = CatererEntity.builder()
                    .name("Nutriflow Mətbəxi")
                    .email("caterer@nutriflow.com")
                    .password(passwordEncoder.encode("caterer123"))
                    .phone("+994502222222")
                    .address("Bakı şəhəri, Nizami küç. 45")
                    .status(CatererStatus.ACTIVE)
                    .build();
            catererRepository.save(caterer);
            System.out.println(">>> Default Caterer Yaradıldı: caterer@nutriflow.com");
        }
    }
}