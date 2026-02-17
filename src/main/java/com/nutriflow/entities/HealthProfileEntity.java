package com.nutriflow.entities;

import com.nutriflow.enums.GoalType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "health_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfileEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "height", nullable = false)
    private Double height;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal", nullable = false)
    private GoalType goal;

    @Column(name = "restrictions", columnDefinition = "TEXT")
    private String restrictions;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @OneToMany(mappedBy = "healthProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicalFileEntity> medicalFiles = new ArrayList<>();
}
