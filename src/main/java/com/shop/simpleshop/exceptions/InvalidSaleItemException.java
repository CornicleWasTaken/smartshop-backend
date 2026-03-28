package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when a sale item contains invalid data.
 * Typically thrown with HTTP 400 Bad Request status.
 * Examples: negative quantity, zero price, invalid product reference.
 */
public class InvalidSaleItemException extends RuntimeException {

    public InvalidSaleItemException(String message) {
        super(message);
    }
}
