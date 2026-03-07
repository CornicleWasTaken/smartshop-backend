package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.InventoryTransactionRequestDTO;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.inventory.InventoryTransaction;
import com.shop.simpleshop.inventory.TransactionType;
import com.shop.simpleshop.repository.InventoryTransactionRepository;
import com.shop.simpleshop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryTransactionRepository transactionRepository, InventoryTransactionRepository inventoryTransactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    public List<InventoryTransaction> getHistory(Long productId) {
        return inventoryTransactionRepository.findByProductProductIdOrderByTimestampDesc(productId);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public void processTransaction(InventoryTransactionRequestDTO request) {

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int newStock = product.getStockQuantity();

        if (request.getType() == TransactionType.IN) {
            newStock += request.getQuantity();
        }

        if (request.getType() == TransactionType.OUT) {
            newStock -= request.getQuantity();
        }

        if (request.getType() == TransactionType.ADJUSTMENT) {
            newStock = request.getQuantity();
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        if (product.getStockQuantity() <= product.getLowStockThreshold()) {
            System.out.println("⚠ LOW STOCK: " + product.getName() +
                    " | Remaining: " + product.getStockQuantity());
        }

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setQuantity(request.getQuantity());
        transaction.setType(request.getType());
        transaction.setReason(request.getReason());
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
    }
}
