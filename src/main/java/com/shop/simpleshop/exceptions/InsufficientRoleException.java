package com.shop.simpleshop.exceptions;

/**
 * Thrown when the current user's role does not permit an action
 * (HTTP 403 Forbidden). Distinct from an unauthenticated 401.
 */
public class InsufficientRoleException extends RuntimeException {

    public InsufficientRoleException(String message) {
        super(message);
    }
}