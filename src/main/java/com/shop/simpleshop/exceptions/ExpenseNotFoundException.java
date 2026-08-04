package com.shop.simpleshop.exceptions;

/**
 * Thrown when an expense with the given ID does not exist (HTTP 404 Not Found).
 */
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long expenseId) {
        super("Expense with ID " + expenseId + " was not found");
    }
}
