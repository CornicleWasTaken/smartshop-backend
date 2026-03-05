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

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "PRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "NAME",nullable = false)
    private String name;

    @Column(name = "SKU",nullable = false, unique = true)
    private String sku;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "STOCK_QUANTITY")
    private Integer stockQuantity;
}
