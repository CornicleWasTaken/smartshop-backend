package com.shop.simpleshop.exceptions;

/**
 * Thrown when opening a cash drawer while another session is already open
 * (HTTP 409 Conflict — a state conflict, not a permission problem).
 */
public class DrawerAlreadyOpenException extends RuntimeException {

    public DrawerAlreadyOpenException(String message) {
        super(message);
    }
}