package com.shop.simpleshop.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Expense counts/amounts grouped by status.
 * Deliberately omits DRAFT, matching the frontend {@code ExpenseStatusSummaryData} type.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseStatusSummaryData {

    private long pendingCount;
    private BigDecimal pendingAmount;
    private long approvedCount;
    private BigDecimal approvedAmount;
    private long paidCount;
    private BigDecimal paidAmount;
    private long rejectedCount;
    private BigDecimal rejectedAmount;
}
