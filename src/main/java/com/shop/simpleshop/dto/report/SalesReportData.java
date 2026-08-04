package com.shop.simpleshop.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sales report. Matches the frontend {@code SalesReportData} type.
 */
@Getter
@Setter
@NoArgsConstructor
public class SalesReportData {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime periodEnd;

    private BigDecimal totalSales;
    private long totalTransactions;
    private BigDecimal averageTransactionValue;
    private long totalItemsSold;
    private List<DailySalesData> dailyBreakdown;
    private List<ProductSalesData> topProducts;
    private List<CategorySalesData> categoryBreakdown;
}
