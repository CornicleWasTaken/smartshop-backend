package com.shop.simpleshop.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Per-day sales aggregation for the sales report.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesData {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private long transactions;
    private BigDecimal amount;
    private BigDecimal averageValue;
}
