package com.shop.simpleshop.controllers;

import java.net.URI;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shop.simpleshop.dto.ProductRequestDTO;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.services.ProductService;

import jakarta.validation.Valid;

/**
 * REST Controller for managing products.
 * Provides endpoints for CRUD operations on products.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Endpoints for managing products in the inventory system")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    /**
     * Creates a new product.
     *
     * @param request the product creation request DTO with validation
     * @return the created product with HTTP 201 status
     */
    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequestDTO request) {
        Product product = mapToEntity(request);
        Product saved = service.create(product);

        return ResponseEntity
                .created(URI.create("/api/products/" + saved.getProductId()))
                .body(saved);
    }

    /**
     * Retrieves all products with pagination.
     *
     * @param pageable pagination parameters
     * @return page of products
     */
    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public Page<Product> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return the product entity
     */

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public Product getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Updates an existing product.
     *
     * @param id the product ID to update
     * @param request the product update request DTO with validation
     * @return the updated product
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing product")
    public Product update(@PathVariable Long id,
                          @Valid @RequestBody ProductRequestDTO request) {

        Product product = mapToEntity(request);
        return service.update(id, product);
    }

    /**
     * Deletes a product.
     *
     * @param id the product ID to delete
     * @return HTTP 204 No Content status
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Maps ProductRequestDTO to Product entity.
     *
     * @param request the DTO to map
     * @return the mapped Product entity
     */
    @SuppressWarnings("unused")
    private Product mapToEntity(ProductRequestDTO request) {
        Product p = new Product();
        p.setName(request.getName());
        p.setSku(request.getSku());
        p.setPrice(request.getPrice());
        p.setStockQuantity(request.getStockQuantity());
        p.setLowStockThreshold(request.getStockQuantity() / 2); // Set threshold as 50% of initial stock
        return p;
    }
}
