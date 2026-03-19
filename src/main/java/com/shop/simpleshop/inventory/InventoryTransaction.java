package com.shop.simpleshop.inventory;

import com.shop.simpleshop.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing an inventory transaction record.
 * Tracks all stock movements (IN, OUT, ADJUSTMENT) with timestamps and reasons.
 */
@Entity
@Getter
@Setter
public class InventoryTransaction {

    /**
     * Unique identifier for the transaction
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the product involved in the transaction
     */
    @ManyToOne
    private Product product;

    /**
     * Quantity involved in the transaction
     */
    private Integer quantity;

    /**
     * Type of transaction: IN (increase), OUT (decrease), or ADJUSTMENT (manual set)
     */
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    /**
     * Reason for the transaction (e.g., "Purchase", "Damage", "Physical Count")
     */
    private String reason;

    /**
     * Timestamp when the transaction was recorded
     */
    private LocalDateTime timestamp;
}

