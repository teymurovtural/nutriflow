package com.nutriflow.enums;

public enum MenuStatus {
    DRAFT,        // Qaralama (dietitian-ın hazırlama mərhələsi)
    SUBMITTED,    // Təqdim olundu (ama onay gözlənilir)
    PREPARING,
    APPROVED,     // Təsdiqlədi (user-ə göstəriləcək)
    REJECTED,     // Rədd olundu (səbəb: rejectionReason)
    CANCELLED     // Ləğv olundu
}