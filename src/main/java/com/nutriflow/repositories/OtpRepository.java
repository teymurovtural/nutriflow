package com.nutriflow.repositories;

import com.nutriflow.entities.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    // Find by email and code (used to update SQL status during verification)
    Optional<OtpEntity> findByEmailAndCode(String email, String code);

    // Can be used in the future to find the last active code for this email
    Optional<OtpEntity> findFirstByEmailOrderByCreatedAtDesc(String email);

    @Query("SELECT o FROM OtpEntity o WHERE o.email = :email AND o.isUsed = false ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpEntity> findLatestByEmail(@Param("email") String email);
}