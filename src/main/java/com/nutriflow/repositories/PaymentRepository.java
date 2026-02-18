package com.nutriflow.repositories;

import com.nutriflow.entities.PaymentEntity;
import com.nutriflow.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    boolean existsByTransactionRef(String transactionRef);

    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.status = com.nutriflow.enums.PaymentStatus.SUCCESS")
    Double sumTotalRevenue();

    Page<PaymentEntity> findAllByOrderByPaymentDateDesc(Pageable pageable); // For latest payments list

    @Query("SELECT SUM(p.amount) FROM PaymentEntity p WHERE p.status = :status")
    Double getTotalRevenueByStatus(@Param("status") PaymentStatus status);

    // Inside PaymentRepository
    @Query(value = "SELECT TO_CHAR(p.payment_date, 'Month'), SUM(p.amount) " +
            "FROM payments p " +
            "WHERE p.status = 'SUCCESS' " +
            "AND p.payment_date BETWEEN :start AND :end " +
            "GROUP BY TO_CHAR(p.payment_date, 'Month')", nativeQuery = true)
    List<Object[]> getMonthlyRevenueCustomRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}