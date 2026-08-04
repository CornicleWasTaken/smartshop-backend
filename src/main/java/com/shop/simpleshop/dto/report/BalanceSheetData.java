package com.shop.simpleshop.dto.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Balance sheet.
 *
 * Synthesized from the available data (documented in README): the system has no
 * cash/accounts-payable/equity ledgers, so {@code cash == retainedEarnings} equals
 * lifetime sales minus lifetime recognized expenses, and
 * {@code equity = retainedEarnings + inventoryValue} (inventory financed by equity),
 * which keeps the sheet balanced by construction.
 */
@Getter
@Setter
@NoArgsConstructor
public class BalanceSheetData {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate asOfDate;

    private BigDecimal currentAssets;
    private BigDecimal inventoryValue;
    private BigDecimal cash;
    private BigDecimal totalAssets;

    private BigDecimal currentLiabilities;
    private BigDecimal accountsPayable;
    private BigDecimal totalLiabilities;

    private BigDecimal equity;
    private BigDecimal retainedEarnings;
    private BigDecimal totalEquity;
    private BigDecimal totalLiabilitiesAndEquity;
    private boolean balanceSheetBalanced;
}
