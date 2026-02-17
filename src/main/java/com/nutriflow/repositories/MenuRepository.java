package com.nutriflow.repositories;

import com.nutriflow.entities.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    // UniqueConstraint-ə uyğun yoxlama (Bir userin eyni ayda 2 menyusu ola bilməz)
    Optional<MenuEntity> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    void deleteByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
    // Müəyyən bir dietoloqun neçə menyu hazırladığını tapmaq üçün
    long countByDietitianId(Long dietitianId);

    // Menyu cədvəlində olan qeydlərin sayını tapmaq üçün
    long count();

}