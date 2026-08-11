package com.shop.simpleshop.pos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A cash-drawer shift: who opened it, when, and the opening/closing float.
 * Closing variance is computed at close time by {@code CashDrawerService}.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "CASH_DRAWER_SESSION")
public class CashDrawerSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DRAWER_ID")
    private Long drawerId;

    @Column(name = "CASHIER", nullable = false, length = 200)
    private String cashier;

    @Column(name = "OPENED_AT", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "CLOSED_AT")
    private LocalDateTime closedAt;

    @Column(name = "OPENING_CASH", precision = 10, scale = 2)
    private BigDecimal openingCash;

    @Column(name = "CLOSING_CASH", precision = 10, scale = 2)
    private BigDecimal closingCash;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private DrawerStatus status;
}