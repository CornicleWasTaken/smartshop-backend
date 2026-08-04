package com.shop.simpleshop.repository;

import com.shop.simpleshop.entity.Expense;
import com.shop.simpleshop.expenses.ExpenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.expenseDate between :start and :end")
    BigDecimal sumAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select max(e.amount) from Expense e where e.expenseDate between :start and :end")
    BigDecimal maxAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
            select e.status, count(e), coalesce(sum(e.amount), 0)
            from Expense e
            where e.expenseDate between :start and :end
            group by e.status
            """)
    List<Object[]> summarizeByStatusBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    Page<Expense> findByExpenseDateBetweenOrderByAmountDesc(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Sum of "recognized" expenses (APPROVED or PAID) over all time.
     * Used by the balance sheet / profit & loss, which only count recognized outflows.
     */
    @Query("""
            select coalesce(sum(e.amount), 0)
            from Expense e
            where e.status in (com.shop.simpleshop.expenses.ExpenseStatus.APPROVED,
                               com.shop.simpleshop.expenses.ExpenseStatus.PAID)
            """)
    BigDecimal sumRecognizedLifetime();

    /**
     * Sum of recognized expenses within a date range (used by reports).
     */
    @Query("""
            select coalesce(sum(e.amount), 0)
            from Expense e
            where e.status in (com.shop.simpleshop.expenses.ExpenseStatus.APPROVED,
                               com.shop.simpleshop.expenses.ExpenseStatus.PAID)
              and e.expenseDate between :start and :end
            """)
    BigDecimal sumRecognizedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
