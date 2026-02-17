package com.nutriflow.repositories;

import com.nutriflow.entities.SubscriptionEntity;
import com.nutriflow.entities.UserEntity;
import com.nutriflow.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    Optional<SubscriptionEntity> findByUser(UserEntity user);
    long countByStatus(SubscriptionStatus status);

    // SubscriptionRepository.java-ya əlavə et:

    List<SubscriptionEntity> findByStatusAndEndDateBefore(
            SubscriptionStatus status,
            LocalDate date
    );

    List<SubscriptionEntity> findByStatusAndEndDate(
            SubscriptionStatus status,
            LocalDate date
    );

    Optional<SubscriptionEntity> findByUserId(Long userId);

}