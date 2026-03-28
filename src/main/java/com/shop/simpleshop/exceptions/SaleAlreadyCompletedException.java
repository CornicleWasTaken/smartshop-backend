package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when attempting to modify a sale that has already been completed.
 * Typically thrown with HTTP 400 Bad Request status.
 */
public class SaleAlreadyCompletedException extends RuntimeException {

    /**
     * Constructs a SaleAlreadyCompletedException with a descriptive message containing the sale ID.
     *
     * @param saleId the ID of the sale that is already completed
     */
    public SaleAlreadyCompletedException(Long saleId) {
        super("Sale with id: " + saleId + " is already completed and cannot be modified");
    }
}
