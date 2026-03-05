package com.shop.simpleshop.services;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.exceptions.ProductNotFoundException;
import com.shop.simpleshop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product create(Product product) {
        return repo.save(product);
    }

    public Page<Product> getAll(Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Product getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product update(Long id, Product updated) {
        Product existing = getById(id);

        existing.setName(updated.getName());
        existing.setSku(updated.getSku());
        existing.setPrice(updated.getPrice());
        existing.setStockQuantity(updated.getStockQuantity());

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.delete(getById(id));
    }
}