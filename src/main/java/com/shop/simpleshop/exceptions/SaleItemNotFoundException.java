package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a requested sale item cannot be found in the system.
 * Typically thrown with HTTP 404 Not Found status.
 */
public class SaleItemNotFoundException extends RuntimeException {

    /**
     * Constructs a SaleItemNotFoundException with a descriptive message containing the sale item ID.
     *
     * @param itemId the ID of the sale item that was not found
     */
    public SaleItemNotFoundException(Long itemId) {
        super("Sale item not found with id: " + itemId);
    }
}
