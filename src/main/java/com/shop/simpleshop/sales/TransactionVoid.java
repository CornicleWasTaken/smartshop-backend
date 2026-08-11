package com.shop.simpleshop.sales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit-style record of a voided sale or sale item. A line-item void links the
 * {@code SaleItem}; a whole-sale void leaves it {@code null} and cancels the sale.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TRANSACTION_VOID")
public class TransactionVoid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VOID_ID")
    private Long voidId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "SALE_ID", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SALE_ITEM_ID")
    private SaleItem saleItem;

    @Column(name = "CASHIER", nullable = false, length = 200)
    private String cashier;

    @Column(name = "REASON", length = 500)
    private String reason;

    @Column(name = "OVERRIDE_BY", length = 200)
    private String overrideBy;

    @Column(name = "VOIDED_AT", nullable = false)
    private LocalDateTime voidedAt;
}