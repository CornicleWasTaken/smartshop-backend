package com.shop.simpleshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shop.simpleshop.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProducts();

    /**
     * Finds a product by its unique SKU.
     *
     * @param sku the stock keeping unit
     * @return an Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Finds a product by its name.
     *
     * @param name the product name
     * @return an Optional containing the product if found
     */
    Optional<Product> findByName(String name);
}
