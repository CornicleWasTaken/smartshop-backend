package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when an inventory transaction contains invalid parameters
 * or violates business rules.
 */
public class InvalidInventoryTransactionException extends RuntimeException {
    public InvalidInventoryTransactionException(String message) {
        super(message);
    }
}
