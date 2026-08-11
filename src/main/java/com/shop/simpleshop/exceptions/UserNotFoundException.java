package com.shop.simpleshop.exceptions;

/**
 * Thrown when a requested user does not exist (HTTP 404 Not Found).
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
}