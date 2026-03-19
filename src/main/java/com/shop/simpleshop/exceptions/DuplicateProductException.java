package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when attempting to create a product with a duplicate SKU or name.
 */
public class DuplicateProductException extends RuntimeException {
    public DuplicateProductException(String message) {
        super(message);
    }
}
