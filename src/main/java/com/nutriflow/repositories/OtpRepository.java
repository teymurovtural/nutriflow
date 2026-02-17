package com.nutriflow.repositories;

import com.nutriflow.entities.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    // Email və koda görə tapmaq (verify zamanı SQL statusunu yeniləmək üçün)
    Optional<OtpEntity> findByEmailAndCode(String email, String code);

    // İstəsən gələcəkdə bu email-ə aid olan sonuncu aktiv kodu tapmaq üçün istifadə edə bilərsən
    Optional<OtpEntity> findFirstByEmailOrderByCreatedAtDesc(String email);
}