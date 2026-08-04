package com.shop.simpleshop.exceptions;

/**
 * Thrown when attempting to delete a product that is referenced by sale items
 * or inventory transactions. Deleting it would violate referential integrity,
 * so the request is rejected with HTTP 409 Conflict.
 */
public class ProductDeleteConflictException extends RuntimeException {

    public ProductDeleteConflictException(Long productId) {
        super("Product with ID " + productId + " cannot be deleted because it is referenced by sales or inventory transactions");
    }
}
