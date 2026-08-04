package com.shop.simpleshop.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Profit & loss statement.
 *
 * Approximations (documented in README): the system has no product cost/COGS and no
 * returns model, so {@code grossProfit = netSales - totalExpenses} and
 * {@code operatingProfit = netProfit = grossProfit}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProfitLossData {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime periodStart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime periodEnd;

    private BigDecimal totalSales;
    private BigDecimal salesReturns;
    private BigDecimal netSales;
    private BigDecimal totalExpenses;
    private BigDecimal operatingExpenses;
    private BigDecimal grossProfit;
    private BigDecimal operatingProfit;
    private BigDecimal netProfit;
    private BigDecimal grossMarginPercentage;
    private BigDecimal netMarginPercentage;
}
