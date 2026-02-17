package com.nutriflow.repositories;

import com.nutriflow.entities.DietitianEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DietitianRepository extends JpaRepository<DietitianEntity, Long> {

    Optional<DietitianEntity> findByEmail(String email);

    // Aktiv olan və ən az pasiyenti (user-i) olan dietoloqu tapmaq üçün
    @Query("SELECT d FROM DietitianEntity d WHERE d.isActive = true ORDER BY size(d.users) ASC")
    Optional<DietitianEntity> findFirstByIsActiveTrueOrderByUsersSizeAsc();

    boolean existsByEmail(String email);

    long countByIsActiveTrue();

    Page<DietitianEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName, Pageable pageable);

    @Query("SELECT d FROM DietitianEntity d WHERE " +
            "LOWER(d.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(d.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<DietitianEntity> searchDietitians(@Param("query") String query, Pageable pageable);


}