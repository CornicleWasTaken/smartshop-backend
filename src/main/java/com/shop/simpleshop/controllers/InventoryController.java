package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.InventoryTransactionRequestDTO;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.inventory.InventoryTransaction;
import com.shop.simpleshop.services.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> processTransaction(
            @Valid @RequestBody InventoryTransactionRequestDTO request) {

        inventoryService.processTransaction(request);
        return ResponseEntity.ok("Transaction processed");
    }

    @GetMapping("/history/{productId}")
    public List<InventoryTransaction> getInventoryHistory(@PathVariable Long productId) {
        return inventoryService.getHistory(productId);
    }

    @GetMapping("/low-stock")
    public List<Product> getLowStockProducts() {
        return inventoryService.getLowStockProducts();
    }

}
