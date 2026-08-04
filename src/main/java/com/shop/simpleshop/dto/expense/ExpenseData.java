package com.shop.simpleshop.dto.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shop.simpleshop.expenses.ExpenseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Expense representation returned to the frontend.
 * Field names must match the frontend {@code ExpenseData} type exactly.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExpenseData {

    private Long expenseId;
    private String description;
    private BigDecimal amount;
    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expenseDate;

    private String vendor;
    private boolean receiptAttached;
    private ExpenseStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedDate;
}
