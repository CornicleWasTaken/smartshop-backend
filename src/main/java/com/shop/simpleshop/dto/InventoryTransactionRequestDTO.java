package com.shop.simpleshop.dto;

import com.shop.simpleshop.inventory.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for creating inventory transactions.
 * Defines the structure for IN, OUT, and ADJUSTMENT transactions.
 */
@Getter
@Setter
public class InventoryTransactionRequestDTO {

    /**
     * The ID of the product involved in the transaction - must not be null
     */
    @NotNull(message = "Product ID is required")
    private Long productId;

    /**
     * The quantity of the transaction - must be positive
     */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    /**
     * The type of transaction - IN, OUT, or ADJUSTMENT - must not be null
     */
    @NotNull(message = "Transaction type is required (IN, OUT, or ADJUSTMENT)")
    private TransactionType type;

    /**
     * Reason for the transaction (optional)
     */
    private String reason;
}


