package com.hospital.hms.security;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_HOSPITAL =
            new ThreadLocal<>();

    private TenantContext() {
        // Utility class
    }

    public static void setHospitalId(Long hospitalId) {
        CURRENT_HOSPITAL.set(hospitalId);
    }

    public static Long getHospitalId() {
        return CURRENT_HOSPITAL.get();
    }

    public static void clear() {
        CURRENT_HOSPITAL.remove();
    }
}