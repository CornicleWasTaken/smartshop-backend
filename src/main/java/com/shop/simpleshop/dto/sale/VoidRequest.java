package com.shop.simpleshop.dto.sale;

import jakarta.validation.constraints.Size;

public record VoidRequest(
        @Size(max = 500, message = "reason must be 500 characters or fewer")
        String reason
) {
}