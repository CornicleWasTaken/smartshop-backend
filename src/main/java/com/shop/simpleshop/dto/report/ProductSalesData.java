package com.shop.simpleshop.dto.report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Per-product sales aggregation for the sales report.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSalesData {

    private Long productId;
    private String productName;
    private long quantitySold;
    private BigDecimal totalRevenue;
    private double percentageOfSales;
}
