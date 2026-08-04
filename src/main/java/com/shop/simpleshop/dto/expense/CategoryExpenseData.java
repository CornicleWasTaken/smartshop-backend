package com.shop.simpleshop.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Per-category expense aggregation for the expense report.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpenseData {

    private String category;
    private BigDecimal amount;
    private long items;
    private double percentage;
}
