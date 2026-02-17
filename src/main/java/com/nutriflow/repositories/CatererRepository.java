package com.nutriflow.repositories;

import com.nutriflow.entities.CatererEntity;
import com.nutriflow.enums.CatererStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatererRepository extends JpaRepository<CatererEntity, Long> {
    Optional<CatererEntity> findByEmail(String email);
    // Aktiv olan ilk (və yeganə) caterer-i tapır
    Optional<CatererEntity> findFirstByStatus(CatererStatus status);
    boolean existsByEmail(String email);
    // Şirkət adına görə axtarış
    Page<CatererEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

}