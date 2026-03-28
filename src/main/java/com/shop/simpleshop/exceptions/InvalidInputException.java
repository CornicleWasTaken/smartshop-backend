package com.shop.simpleshop.exceptions;

/**
 * Exception thrown when the input data provided to an API endpoint is invalid or incomplete.
 */
public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}
