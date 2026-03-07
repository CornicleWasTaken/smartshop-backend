package com.shop.simpleshop.controllers;

import java.net.URI;

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

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequestDTO request) {
        Product product = mapToEntity(request);
        Product saved = service.create(product);

        return ResponseEntity
                .created(URI.create("/products/" + saved.getProductId()))
                .body(saved);
    }

    @GetMapping
    public Page<Product> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id,
                          @RequestBody ProductRequestDTO request) {

        Product product = mapToEntity(request);
        return service.update(id, product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Product mapToEntity(ProductRequestDTO request) {
        Product p = new Product();
        p.setName(request.getName());
        p.setSku(request.getSku());
        p.setPrice(request.getPrice());
        p.setStockQuantity(request.getStockQuantity());
        return p;
    }
}
