package com.nutriflow.entities;

import com.nutriflow.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"batch_id", "day_number", "meal_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private MenuBatchEntity batch;

    @Column(name = "day_number", nullable = false)
    private Integer day;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "protein")
    private Double protein;

    @Column(name = "carbs")
    private Double carbs;

    @Column(name = "fats")
    private Double fats;
}