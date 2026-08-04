package com.shop.simpleshop.entity;

import com.shop.simpleshop.expenses.ExpenseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Expense entity representing an outgoing business expense.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "EXPENSE")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EXPENSE_ID")
    private Long expenseId;

    @Column(name = "DESCRIPTION", nullable = false, length = 500)
    private String description;

    @Column(name = "AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "CATEGORY", nullable = false, length = 100)
    private String category;

    @Column(name = "EXPENSE_DATE", nullable = false)
    private LocalDateTime expenseDate;

    @Column(name = "VENDOR", length = 200)
    private String vendor;

    @Column(name = "RECEIPT_ATTACHED", nullable = false)
    private boolean receiptAttached;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ExpenseStatus status;

    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE", nullable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdDate = now;
        updatedDate = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
