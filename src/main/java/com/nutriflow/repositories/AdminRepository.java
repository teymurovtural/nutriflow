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
     * Used to check whether the email is unique when creating an admin
     */
    boolean existsByEmail(String email);

    /**
     * Fetch admins by role (for future SuperAdmin/Admin separation)
     */
    List<AdminEntity> findAllByRole(Role role);

    Page<AdminEntity> findAllByIsSuperAdminFalse(Pageable pageable);
}