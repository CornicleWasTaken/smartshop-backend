package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a sale cancellation fails due to business rule violations.
 * Typically thrown with HTTP 400 Bad Request status.
 * Examples: sale already refunded, items already shipped.
 */
public class SaleCancellationException extends RuntimeException {

    public SaleCancellationException(String message) {
        super(message);
    }
}
