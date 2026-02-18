package com.nutriflow.repositories;

import com.nutriflow.entities.ActivityLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {

    Page<ActivityLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

}