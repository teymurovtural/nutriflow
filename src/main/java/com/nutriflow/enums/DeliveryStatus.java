package com.nutriflow.enums;

public enum DeliveryStatus {
    PENDING,
    IN_PROGRESS,    // Being prepared
    READY,          // Ready, heading out for delivery
    ON_THE_WAY,     // On the way
    DELIVERED,      // Delivered
    FAILED          // Failed Delivery
}