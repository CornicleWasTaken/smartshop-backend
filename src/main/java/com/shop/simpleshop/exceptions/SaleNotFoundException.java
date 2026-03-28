package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a requested sale cannot be found in the system.
 * Typically thrown with HTTP 404 Not Found status.
 */
public class SaleNotFoundException extends RuntimeException {

    /**
     * Constructs a SaleNotFoundException with a descriptive message containing the sale ID.
     *
     * @param id the ID of the sale that was not found
     */
    public SaleNotFoundException(Long id) {
        super("Sale not found with id: " + id);
    }
}
