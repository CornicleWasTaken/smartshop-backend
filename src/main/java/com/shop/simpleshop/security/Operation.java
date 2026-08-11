package com.shop.simpleshop.security;

/**
 * Fixed vocabulary of auditable sensitive actions, stored as the
 * {@code action} on {@link AuditLogEntry}. Keeps the audit log machine-readable
 * (Raw material for the anomaly rules in Docs/12).
 */
public enum Operation {
    DELETE,
    VOID,
    OPEN_DRAWER,
    CLOSE_DRAWER,
    UPDATE_ROLE,
    VERIFY_MANAGER
}