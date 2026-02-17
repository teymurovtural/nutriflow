package com.nutriflow.entities;
import com.nutriflow.enums.MenuStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "year", "month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietitian_id", nullable = false)
    private DietitianEntity dietitian;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Builder.Default
    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuBatchEntity> batches = new ArrayList<>();

    @Column(name = "dietary_notes", columnDefinition = "TEXT")
    private String dietaryNotes;

}