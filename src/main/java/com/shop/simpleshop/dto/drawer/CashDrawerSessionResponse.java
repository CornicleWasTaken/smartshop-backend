package com.shop.simpleshop.dto.drawer;

import com.shop.simpleshop.pos.CashDrawerSession;
import com.shop.simpleshop.pos.DrawerStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Serialized cash-drawer session. {@code variance} is computed: for a closed
 * drawer it is {@code closingCash - openingCash} (payments/refunds module does
 * not exist yet, so this is a documented approximation); for an open drawer it
 * is {@code null}.
 */
public record CashDrawerSessionResponse(
        Long drawerId,
        String cashier,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        BigDecimal openingCash,
        BigDecimal closingCash,
        BigDecimal variance,
        DrawerStatus status
) {

    public static CashDrawerSessionResponse from(CashDrawerSession session) {
        BigDecimal variance = null;
        if (session.getStatus() == DrawerStatus.CLOSED
                && session.getOpeningCash() != null
                && session.getClosingCash() != null) {
            variance = session.getClosingCash().subtract(session.getOpeningCash());
        }
        return new CashDrawerSessionResponse(
                session.getDrawerId(),
                session.getCashier(),
                session.getOpenedAt(),
                session.getClosedAt(),
                session.getOpeningCash(),
                session.getClosingCash(),
                variance,
                session.getStatus()
        );
    }
}