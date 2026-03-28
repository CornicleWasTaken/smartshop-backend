package com.shop.simpleshop.services;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.exceptions.DuplicateProductException;
import com.shop.simpleshop.exceptions.ProductNotFoundException;
import com.shop.simpleshop.repository.ProductRepository;

/**
 * Service for managing product CRUD operations.
 * Handles all business logic related to product management.
 */
@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    /**
     * Creates a new product in the system.
     * Validates that SKU and name are unique before creating.
     *
     * @param product the product entity to be created
     * @return the saved product with generated ID
     * @throws DuplicateProductException if a product with the same SKU or name already exists
     */
    public Product create(Product product) {
        // Validate SKU uniqueness
        if (repo.findBySku(product.getSku()).isPresent()) {
            throw new DuplicateProductException(
                    "A product with SKU '" + product.getSku() + "' already exists");
        }

        // Validate name uniqueness
        if (repo.findByName(product.getName()).isPresent()) {
            throw new DuplicateProductException(
                    "A product with name '" + product.getName() + "' already exists");
        }

        return repo.save(product);
    }

    // ...existing code...
    public Page<Product> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    // ...existing code...
    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // ...existing code...
    public Product update(Long id, Product updated) {
        Product existing = getById(id);

        existing.setName(updated.getName());
        existing.setSku(updated.getSku());
        existing.setPrice(updated.getPrice());
        existing.setStockQuantity(updated.getStockQuantity());

        return repo.save(existing);
    }

    // ...existing code...
    public void delete(Long id) {
        repo.delete(getById(id));
    }
}