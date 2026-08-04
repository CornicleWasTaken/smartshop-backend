package com.shop.simpleshop.repository;

import com.shop.simpleshop.sales.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    boolean existsByProduct_ProductId(Long productId);

    long countByProduct_ProductId(Long productId);
}
