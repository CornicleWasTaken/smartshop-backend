package com.shop.simpleshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Product entity representing a product in the shop system.
 * Stores product information including pricing and inventory details.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "PRODUCT")
public class Product {

    /**
     * Unique identifier for the product
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    /**
     * Name of the product
     */
    @Column(name = "NAME", nullable = false)
    private String name;

    /**
     * Stock Keeping Unit - unique identifier for product tracking
     */
    @Column(name = "SKU", nullable = false, unique = true)
    private String sku;

    /**
     * Price of the product
     */
    @Column(name = "PRICE", nullable = false)
    private Double price;

    /**
     * Current stock quantity in inventory
     */
    @Column(name = "STOCK_QUANTITY", nullable = false)
    private Integer stockQuantity;

    /**
     * Threshold below which the product is considered low stock
     */
    @Column(name = "LOW_STOCK_THRESHOLD", nullable = false)
    private Integer lowStockThreshold;
}


