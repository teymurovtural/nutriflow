package com.nutriflow.repositories;

import com.nutriflow.entities.MenuItemEntity;
import com.nutriflow.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItemEntity, Long> {

    @Modifying
    @Query("DELETE FROM MenuItemEntity m WHERE m.batch.id = :batchId AND m.day = :day AND m.mealType = :mealType")
    void deleteByBatchIdAndDayAndMealType(Long batchId, Integer day, MealType mealType);

    @Modifying
    @Query("DELETE FROM MenuItemEntity m WHERE m.batch.id = :batchId AND m.day = :day")
    void deleteByBatchIdAndDay(@Param("batchId") Long batchId, @Param("day") Integer day);

    void deleteByBatchId(Long batchId);

    Optional<MenuItemEntity> findByBatchIdAndDayAndMealType(Long batchId, Integer day, MealType mealType);

}