package com.nutriflow.repositories;

import com.nutriflow.entities.MedicalFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalFileRepository extends JpaRepository<MedicalFileEntity, Long> {

    List<MedicalFileEntity> findByHealthProfileId(Long healthProfileId);

}