package com.shop.simpleshop.dto;

import com.shop.simpleshop.inventory.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryTransactionRequestDTO {
    @NotNull
    private Long productId;

    @NotNull
    private Integer quantity;

    @NotNull
    private TransactionType type;

    private String reason;
}
