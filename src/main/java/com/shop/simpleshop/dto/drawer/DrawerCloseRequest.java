package com.shop.simpleshop.dto.drawer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record DrawerCloseRequest(
        @NotNull(message = "closingCash is required")
        @PositiveOrZero(message = "closingCash must be zero or positive")
        BigDecimal closingCash
) {
}