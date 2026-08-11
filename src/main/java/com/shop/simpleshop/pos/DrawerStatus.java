package com.shop.simpleshop.pos;

/**
 * Lifecycle of a cash-drawer session. The system sets {@link #CLOSED} on end-of-shift.
 */
public enum DrawerStatus {
    OPEN,
    CLOSED
}