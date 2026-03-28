package com.shop.simpleshop.repository;

import com.shop.simpleshop.inventory.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByProductProductIdOrderByTimestampDesc(Long productId);
}
