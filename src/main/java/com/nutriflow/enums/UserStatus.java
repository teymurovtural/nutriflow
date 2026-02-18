package com.nutriflow.enums;

public enum UserStatus {
    REGISTERED,       // Initial registration (Cold lead)
    VERIFIED,         // After email OTP is confirmed
    DATA_SUBMITTED,   // Health data has been entered (Qualified lead)
    ACTIVE,           // Payment completed (Active subscriber)
    EXPIRED           // Subscription period has ended
}