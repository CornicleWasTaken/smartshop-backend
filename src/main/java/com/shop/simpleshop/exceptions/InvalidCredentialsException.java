package com.shop.simpleshop.exceptions;

/**
 * Thrown when login credentials do not match any known user (HTTP 401 Unauthorized).
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username/email or password");
    }
}
