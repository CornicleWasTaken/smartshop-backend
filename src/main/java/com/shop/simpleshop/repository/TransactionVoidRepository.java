package com.shop.simpleshop.repository;

import com.shop.simpleshop.sales.TransactionVoid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionVoidRepository extends JpaRepository<TransactionVoid, Long> {
}