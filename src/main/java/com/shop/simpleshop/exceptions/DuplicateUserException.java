package com.shop.simpleshop.exceptions;

/**
 * Thrown when registering a user whose username or email already exists (HTTP 409 Conflict).
 */
public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}
