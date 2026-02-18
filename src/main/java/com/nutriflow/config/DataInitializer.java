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

    private final AdminRepository adminRepository;
    private final DietitianRepository dietitianRepository;
    private final CatererRepository catererRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 1. CREATE ADMIN
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
            System.out.println(">>> Super Admin created: admin@nutriflow.com");
        }

        // 2. CREATE DIETITIAN
        if (dietitianRepository.findByEmail("diet@nutriflow.com").isEmpty()) {
            DietitianEntity dietitian = DietitianEntity.builder()
                    .firstName("Emiliya")
                    .lastName("Kerimli")
                    .email("diet@nutriflow.com")
                    .password(passwordEncoder.encode("diet123"))
                    .phone("+994501111111")
                    .specialization("Personal Nutrition Specialist")
                    .isActive(true)
                    .build();
            dietitianRepository.save(dietitian);
            System.out.println(">>> Default Dietitian created: diet@nutriflow.com");
        }

        // 3. CREATE CATERER
        if (catererRepository.findByEmail("caterer@nutriflow.com").isEmpty()) {
            CatererEntity caterer = CatererEntity.builder()
                    .name("Nutriflow Kitchen")
                    .email("caterer@nutriflow.com")
                    .password(passwordEncoder.encode("caterer123"))
                    .phone("+994502222222")
                    .address("45 Nizami St, Baku")
                    .status(CatererStatus.ACTIVE)
                    .build();
            catererRepository.save(caterer);
            System.out.println(">>> Default Caterer created: caterer@nutriflow.com");
        }
    }
}