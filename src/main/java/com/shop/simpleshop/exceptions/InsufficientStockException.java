package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when attempting to deduct more stock than currently available.
 * Typically thrown during OUT type inventory transactions when quantity exceeds current stock.
 */
public class InsufficientStockException extends RuntimeException {

    /**
     * Constructs an InsufficientStockException with a descriptive message.
     *
     * @param message descriptive message about the insufficient stock situation
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}
