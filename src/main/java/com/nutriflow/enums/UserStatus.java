package com.nutriflow.enums;

public enum UserStatus {
    REGISTERED,       // İlk qeydiyyat (Cold lead)
    VERIFIED,         // Email OTP təsdiqlənəndən sonra
    DATA_SUBMITTED,   // Sağlamlıq məlumatları daxil edilib (Qualified lead)
    ACTIVE,           // Ödəniş tamamlanıb (Active subscriber)
    EXPIRED           // Abunəlik müddəti bitib
}
