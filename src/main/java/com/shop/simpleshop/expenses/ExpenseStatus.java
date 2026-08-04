package com.shop.simpleshop.expenses;

/**
 * Lifecycle status for an expense. Mirrors the frontend {@code ExpenseStatus} type.
 * Note: the report's status summary intentionally omits DRAFT, matching the frontend.
 */
public enum ExpenseStatus {
    PENDING,
    APPROVED,
    PAID,
    REJECTED,
    DRAFT
}
