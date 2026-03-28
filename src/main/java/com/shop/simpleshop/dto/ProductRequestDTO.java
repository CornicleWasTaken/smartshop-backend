package com.shop.simpleshop.dto;

import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for creating and updating products.
 * Contains validation rules for product information.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductRequestDTO {

    /**
     * Product name - must not be blank
     */
    @NotBlank(message = "Product name is required")
    private String name;

    /**
     * Stock Keeping Unit - unique identifier for the product, must not be blank
     */
    @NotBlank(message = "SKU is required")
    private String sku;

    /**
     * Product price - must be positive (greater than 0)
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private Double price;

    /**
     * Initial stock quantity - must be zero or positive
     */
    @NotNull(message = "Stock quantity is required")
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
}

