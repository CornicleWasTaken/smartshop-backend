package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a sale refund fails due to business rule violations.
 * Typically thrown with HTTP 400 Bad Request status.
 * Examples: refund amount exceeds original, items not returnable.
 */
public class SaleRefundException extends RuntimeException {

    public SaleRefundException(String message) {
        super(message);
    }
}
