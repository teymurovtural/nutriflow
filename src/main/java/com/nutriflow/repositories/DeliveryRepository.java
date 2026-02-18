package com.nutriflow.repositories;

import com.nutriflow.entities.DeliveryEntity;
import com.nutriflow.entities.MenuBatchEntity;
import com.nutriflow.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {

    /**
     * Retrieves deliveries for a caterer with search parameters.
     * CAST(... AS string) - resolves the 'function lower(bytea) does not exist' error in PostgreSQL.
     */
    @Query(value = "SELECT d.* FROM deliveries d " +
            "JOIN users u ON u.id = d.user_id " +
            "JOIN addresses a ON a.id = d.address_id " +
            "JOIN menu_batches b ON b.id = d.batch_id " +
            "WHERE d.caterer_id = :catererId " +
            "AND d.delivery_date = :date " +
            "AND (:name IS NULL OR " +
            "     CAST(u.first_name AS TEXT) ILIKE CONCAT('%', :name, '%') OR " +
            "     CAST(u.last_name AS TEXT) ILIKE CONCAT('%', :name, '%')) " +
            "AND (:district IS NULL OR a.district = :district)",
            nativeQuery = true)
    List<DeliveryEntity> searchDeliveries(
            @Param("catererId") Long catererId,
            @Param("date") java.time.LocalDate date,
            @Param("name") String name,
            @Param("district") String district
    );

    // Methods for statistics (matches calls in CatererServiceImpl)
    long countByCatererIdAndDateAndStatus(Long catererId, LocalDate date, DeliveryStatus status);

    long countByCatererIdAndDate(Long catererId, LocalDate date);

    // Number of completed deliveries for user dashboard
    long countByUserIdAndStatus(Long userId, DeliveryStatus status);

    // Full delivery history for a user
    List<DeliveryEntity> findAllByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DeliveryEntity d WHERE d.date < :oneYearAgo")
    int deleteOldDeliveries(@Param("oneYearAgo") LocalDate oneYearAgo);

    boolean existsByUserIdAndBatchIdAndDate(Long userId, Long batchId, LocalDate date);
    List<DeliveryEntity> findByUserIdAndStatus(Long userId, DeliveryStatus status);
    List<DeliveryEntity> findAllByBatchId(Long batchId);
    void deleteAllByBatchId(Long batchId);
    long countByStatus(DeliveryStatus status);
}