package com.nutriflow.entities;

import com.nutriflow.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_actor", columnList = "actor_type,actor_id"),
        @Index(name = "idx_entity", columnList = "entity_type,entity_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type", nullable = false)  // USER, DIETITIAN, CATERER, ADMIN
    private Role role;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "action", nullable = false)
    // E.g: "menu_submitted", "payment_success", "delivery_updated", etc.
    private String action;

    @Column(name = "entity_type")  // Menu, Delivery, Subscription, etc.
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;  // Previous value of the change (as JSON)

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;  // New value of the change (as JSON)

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;  // Additional information

}