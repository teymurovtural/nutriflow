package com.nutriflow.repositories;

import com.nutriflow.entities.AdminEntity;
import com.nutriflow.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Long> {

    Optional<AdminEntity> findByEmail(String email);

    /**
     * Admin yaradılarkən email-in unikal olub-olmadığını yoxlamaq üçün
     */
    boolean existsByEmail(String email);

    /**
     * Roluna görə adminləri gətirmək (Gələcəkdə SuperAdmin/Admin ayrımı üçün)
     */
    List<AdminEntity> findAllByRole(Role role);

    Page<AdminEntity> findAllByIsSuperAdminFalse(Pageable pageable);


}