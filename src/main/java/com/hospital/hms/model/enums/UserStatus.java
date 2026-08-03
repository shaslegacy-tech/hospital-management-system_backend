package com.hospital.hms.model.enums;

public enum UserStatus {
    PENDING,    // Self-registered, awaiting receptionist approval
    ACTIVE,     // Approved and fully active
    REJECTED    // Rejected by receptionist
}
