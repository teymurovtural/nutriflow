package com.nutriflow.repositories;

import com.nutriflow.entities.MedicalFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalFileRepository extends JpaRepository<MedicalFileEntity, Long> {

    List<MedicalFileEntity> findByHealthProfileId(Long healthProfileId);

    Optional<MedicalFileEntity> findByIdAndHealthProfileId(Long id, Long healthProfileId);


}