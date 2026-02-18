package com.nutriflow.repositories;

import com.nutriflow.entities.HealthProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfileEntity, Long> {

    Optional<HealthProfileEntity> findByUserId(Long userId);

}