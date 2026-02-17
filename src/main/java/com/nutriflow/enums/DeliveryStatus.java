package com.nutriflow.enums;

public enum DeliveryStatus {
    PENDING,
    IN_PROGRESS,    // Hazırlanır
    READY,          // Hazırdır, çatdırılmaya gedir
    ON_THE_WAY,     // Yolda
    DELIVERED,      // Çatdırıldı
    FAILED
}