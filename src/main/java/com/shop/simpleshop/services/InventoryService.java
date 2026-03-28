package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.InventoryTransactionRequestDTO;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.exceptions.InsufficientStockException;
import com.shop.simpleshop.exceptions.InvalidInventoryTransactionException;
import com.shop.simpleshop.exceptions.ProductNotFoundException;
import com.shop.simpleshop.inventory.InventoryTransaction;
import com.shop.simpleshop.inventory.TransactionType;
import com.shop.simpleshop.repository.InventoryTransactionRepository;
import com.shop.simpleshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling inventory transactions and stock management.
 * Manages stock movements, validates transactions, and tracks transaction history.
 */
@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryTransactionRepository transactionRepository,
                            InventoryTransactionRepository inventoryTransactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    /**
     * Retrieves the transaction history for a product, ordered by most recent first.
     *
     * @param productId the ID of the product
     * @return list of inventory transactions
     * @throws ProductNotFoundException if product doesn't exist
     */
    public List<InventoryTransaction> getHistory(Long productId) {
        // Validate that product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return inventoryTransactionRepository.findByProductProductIdOrderByTimestampDesc(productId);
    }

    /**
     * Retrieves all products with stock quantity below their threshold.
     *
     * @return list of low stock products
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    /**
     * Processes an inventory transaction (IN, OUT, or ADJUSTMENT).
     *
     * @param request the inventory transaction request containing product ID, quantity, type, and reason
     * @throws ProductNotFoundException if product doesn't exist
     * @throws InvalidInventoryTransactionException if quantity is invalid
     * @throws InsufficientStockException if OUT transaction would result in negative stock
     */
    public void processTransaction(InventoryTransactionRequestDTO request) {

        // Validate request parameters
        validateTransactionRequest(request);

        // Find product
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));

        // Calculate new stock based on transaction type
        int newStock = product.getStockQuantity();

        if (request.getType() == TransactionType.IN) {
            newStock += request.getQuantity();
        } else if (request.getType() == TransactionType.OUT) {
            // Validate sufficient stock before deduction
            if (product.getStockQuantity() < request.getQuantity()) {
                throw new InsufficientStockException(
                        String.format("Cannot deduct %d units. Available stock: %d",
                                request.getQuantity(), product.getStockQuantity()));
            }
            newStock -= request.getQuantity();
        } else if (request.getType() == TransactionType.ADJUSTMENT) {
            newStock = request.getQuantity();
        }

        // Update product stock
        product.setStockQuantity(newStock);
        productRepository.save(product);

        // Log low stock warning
        if (product.getStockQuantity() <= product.getLowStockThreshold()) {
            System.out.println("⚠ LOW STOCK WARNING: " + product.getName() +
                    " | Remaining Stock: " + product.getStockQuantity() +
                    " | Threshold: " + product.getLowStockThreshold());
        }

        // Record the transaction
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setQuantity(request.getQuantity());
        transaction.setType(request.getType());
        transaction.setReason(request.getReason());
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
    }

    /**
     * Validates the inventory transaction request.
     *
     * @param request the transaction request to validate
     * @throws InvalidInventoryTransactionException if validation fails
     */
    private void validateTransactionRequest(InventoryTransactionRequestDTO request) {
        // Validate quantity is positive
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new InvalidInventoryTransactionException(
                    "Transaction quantity must be greater than zero. Provided: " + request.getQuantity());
        }

        // Validate transaction type is present
        if (request.getType() == null) {
            throw new InvalidInventoryTransactionException(
                    "Transaction type is required (IN, OUT, or ADJUSTMENT)");
        }

        // Validate product ID is present
        if (request.getProductId() == null) {
            throw new InvalidInventoryTransactionException(
                    "Product ID is required for transaction");
        }
    }
}
