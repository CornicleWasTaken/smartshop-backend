package com.shop.simpleshop.exceptions;

/**
 * Thrown when a sensitive action violates a business rule (HTTP 403 Forbidden),
 * e.g. voiding a sale that is not in a voidable state.
 */
public class ForbiddenActionException extends RuntimeException {

    public ForbiddenActionException(String message) {
        super(message);
    }
}