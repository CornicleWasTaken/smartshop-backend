package com.shop.simpleshop.dto.drawer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DrawerOpenRequest(
        @NotNull(message = "openingCash is required")
        @Positive(message = "openingCash must be positive")
        BigDecimal openingCash
) {
}