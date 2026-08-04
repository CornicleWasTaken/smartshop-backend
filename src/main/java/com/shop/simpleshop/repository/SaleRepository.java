package com.shop.simpleshop.repository;

import com.shop.simpleshop.sales.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Page<Sale> findAll(Pageable pageable);

    /**
     * Completed sales within a date range, with sale items eagerly fetched.
     * Used by the sales report.
     */
    @Query("""
            select s from Sale s
            join fetch s.items
            where s.saleDate between :start and :end
              and s.status = com.shop.simpleshop.sales.SaleStatus.COMPLETED
            order by s.saleDate
            """)
    List<Sale> findCompletedWithItemsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Total value of all completed sales (all time). Used by the balance sheet.
     */
    @Query("""
            select coalesce(sum(s.totalAmount), 0)
            from Sale s
            where s.status = com.shop.simpleshop.sales.SaleStatus.COMPLETED
            """)
    BigDecimal sumLifetimeCompleted();
}
