package com.nutriflow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalFileEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_profile_id", nullable = false)
    private HealthProfileEntity healthProfile;

    @Column(name = "file_url", nullable = false)
    private String fileUrl; // Link in cloud storage

    @Column(name = "file_name")
    private String fileName; // File name (e.g: blood_test.pdf)

    @Column(name = "file_type")
    private String fileType; // mime-type (e.g: application/pdf)
}