package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a requested product cannot be found in the system.
 * Typically thrown with HTTP 404 Not Found status.
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructs a ProductNotFoundException with a descriptive message containing the product ID.
     *
     * @param id the ID of the product that was not found
     */
    public ProductNotFoundException(Long id) {
        super("Product not found with id: " + id);
    }
}
