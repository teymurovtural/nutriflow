package com.nutriflow.enums;

public enum MenuStatus {
    DRAFT,        // Draft (dietitian's preparation stage)
    SUBMITTED,    // Submitted (awaiting approval)
    PREPARING,
    APPROVED,     // Approved (will be shown to user)
    REJECTED,     // Rejected (reason: rejectionReason)
    CANCELLED     // Cancelled
}