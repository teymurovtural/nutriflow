package com.nutriflow.entities;

import com.nutriflow.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries", indexes = {
        @Index(name = "idx_delivery_user_date", columnList = "user_id,delivery_date"),
        @Index(name = "idx_delivery_caterer_date", columnList = "caterer_id,delivery_date"),
        @Index(name = "idx_delivery_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caterer_id", nullable = false)
    private CatererEntity caterer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address;

    // --- YENİ ƏLAVƏ ETDİYİMİZ HİSSƏ ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private MenuBatchEntity batch; // Çatdırılmanın hansı paketə aid olduğunu bilmək üçün
    // ---------------------------------

    @Column(name = "delivery_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "estimated_delivery_time")
    private String estimatedDeliveryTime;

    @Column(name = "actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "caterer_note", columnDefinition = "TEXT")
    private String catererNote;
}