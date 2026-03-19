package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.InventoryTransactionRequestDTO;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.inventory.InventoryTransaction;
import com.shop.simpleshop.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing inventory transactions and stock levels.
 * Provides endpoints for processing inventory transactions, viewing history, and monitoring stock levels.
 */
@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Endpoints for managing inventory transactions and stock levels")
public class InventoryController {


    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Processes an inventory transaction (IN, OUT, or ADJUSTMENT).
     *
     * @param request the inventory transaction request with validation
     * @return response entity with transaction status
     */
    @Operation(summary = "Process an inventory transaction")
    @PostMapping("/transaction")
    public ResponseEntity<Map<String, String>> processTransaction(
            @Valid @RequestBody InventoryTransactionRequestDTO request) {

        inventoryService.processTransaction(request);

        Map<String, String> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Transaction processed successfully");
        response.put("transactionType", request.getType().toString());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Retrieves the transaction history for a specific product.
     *
     * @param productId the ID of the product
     * @return list of inventory transactions for the product, ordered by most recent first
     */
    @GetMapping("/history/{productId}")
    @Operation(summary = "Get transaction history for a product")
    public List<InventoryTransaction> getInventoryHistory(@PathVariable Long productId) {
        return inventoryService.getHistory(productId);
    }

    /**
     * Retrieves all products with stock quantity below their low stock threshold.
     *
     * @return list of low stock products
     */

    @GetMapping("/low-stock")
    @Operation(summary = "Get products with low stock")
    public List<Product> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

}
