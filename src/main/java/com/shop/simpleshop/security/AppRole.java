package com.shop.simpleshop.security;

/**
 * The effective application roles used for authorization.
 *
 * <p>The legacy {@code STAFF} value stored in {@code APP_USER.ROLE} maps to
 * {@link #CASHIER} on read (see {@link #fromDb(String)}), so the three effective
 * roles are {@code ADMIN}, {@code MANAGER}, and {@code CASHIER}.
 *
 * <p>When building Spring authorities the role appears as {@code ROLE_<name>}
 * (e.g. {@code ROLE_CASHIER}), matching {@code hasRole(...)} expressions.
 */
public enum AppRole {
    ADMIN,
    MANAGER,
    CASHIER;

    /**
     * Maps a persisted role string to an {@code AppRole}.
     *
     * <p>Legacy {@code STAFF} and any unknown value fall back to {@link #CASHIER}
     * (least privilege). This is intentionally fail-safe: an unrecognized role
     * keeps read access without granting write/sensitive permissions.
     */
    /** Whether this role can grant overrides and perform sensitive actions. */
    public boolean isElevated() {
        return this == ADMIN || this == MANAGER;
    }

    public static AppRole fromDb(String role) {
        if (role == null) {
            return CASHIER;
        }
        String normalized = role.trim().toUpperCase();
        return switch (normalized) {
            case "ADMIN", "MANAGER", "CASHIER" -> valueOf(normalized);
            // Legacy value and any unknown values fall back to least privilege.
            default -> CASHIER;
        };
    }
}