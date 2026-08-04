package com.shop.simpleshop.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Per-category sales aggregation for the sales report.
 * Products without a category roll up under "Uncategorized".
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesData {

    private String category;
    private long itemsSold;
    private BigDecimal revenue;
    private double percentageOfSales;
}
