package com.shop.simpleshop.dto.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Expense report summary. Matches the frontend {@code ExpenseReportData} type.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExpenseReportData {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime periodEnd;

    private BigDecimal totalExpenses;
    private long totalItems;
    private BigDecimal averageAmount;
    private BigDecimal largestExpense;
    private List<CategoryExpenseData> categoryBreakdown;
    private List<ExpenseData> topExpenses;
    private ExpenseStatusSummaryData statusSummary;
}
