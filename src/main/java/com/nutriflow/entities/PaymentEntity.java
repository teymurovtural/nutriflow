package com.nutriflow.entities;

import com.nutriflow.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    private SubscriptionEntity subscription;

    @Column(name = "provider", nullable = false)
    private String provider; // Məs: "BANK_CARD", "CASH"

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_ref", unique = true)
    private String transactionRef;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // Xəta mesajları və ya qeydlər üçün
}