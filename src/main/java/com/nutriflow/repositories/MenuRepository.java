package com.nutriflow.repositories;

import com.nutriflow.entities.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    // Validation matching UniqueConstraint (a user cannot have 2 menus in the same month)
    Optional<MenuEntity> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    void deleteByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    // Find how many menus a specific dietitian has prepared
    long countByDietitianId(Long dietitianId);
    // Find the total number of records in the menu table
    long count();
}