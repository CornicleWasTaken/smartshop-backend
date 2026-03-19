package com.shop.simpleshop.services;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shop.simpleshop.entity.Product;
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
     *
     * @param product the product entity to be created
     * @return the saved product with generated ID
     */
    public Product create(Product product) {
        return repo.save(product);
    }

    /**
     * Retrieves all products with pagination support.
     *
     * @param pageable pagination information (page number, size, sort)
     * @return a page of products
     */
    public Page<Product> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * Retrieves a single product by its ID.
     *
     * @param id the product ID
     * @return the product entity
     * @throws ProductNotFoundException if product with given ID doesn't exist
     */
    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    /**
     * Updates an existing product with new information.
     *
     * @param id the product ID to update
     * @param updated the product entity with updated information
     * @return the updated product
     * @throws ProductNotFoundException if product with given ID doesn't exist
     */
    public Product update(Long id, Product updated) {
        Product existing = getById(id);

        existing.setName(updated.getName());
        existing.setSku(updated.getSku());
        existing.setPrice(updated.getPrice());
        existing.setStockQuantity(updated.getStockQuantity());

        return repo.save(existing);
    }

    /**
     * Deletes a product from the system.
     *
     * @param id the product ID to delete
     * @throws ProductNotFoundException if product with given ID doesn't exist
     */
    public void delete(Long id) {
        repo.delete(getById(id));
    }
}