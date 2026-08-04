package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.report.BalanceSheetData;
import com.shop.simpleshop.dto.report.FinancialSummaryData;
import com.shop.simpleshop.dto.report.ProfitLossData;
import com.shop.simpleshop.dto.report.SalesReportData;
import com.shop.simpleshop.services.ReportService;
import com.shop.simpleshop.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Financial reports computed from sales, expenses, and inventory")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    @Operation(summary = "Sales report for a date range")
    public SalesReportData salesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = DateUtils.parseDateTime(startDate, "startDate");
        LocalDateTime end = DateUtils.parseDateTime(endDate, "endDate");
        return reportService.salesReport(defaultStart(start), defaultEnd(end));
    }

    @GetMapping("/profit-loss")
    @Operation(summary = "Profit & loss statement for a date range")
    public ProfitLossData profitLoss(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = DateUtils.parseDateTime(startDate, "startDate");
        LocalDateTime end = DateUtils.parseDateTime(endDate, "endDate");
        return reportService.profitLoss(defaultStart(start), defaultEnd(end));
    }

    @GetMapping("/balance-sheet")
    @Operation(summary = "Balance sheet as of a date")
    public BalanceSheetData balanceSheet(
            @RequestParam(required = false) String asOfDate) {
        LocalDate date = DateUtils.parseDate(asOfDate, "asOfDate");
        return reportService.balanceSheet(date != null ? date : LocalDate.now());
    }

    @GetMapping("/financial-summary")
    @Operation(summary = "Financial summary for a date range")
    public FinancialSummaryData financialSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = DateUtils.parseDateTime(startDate, "startDate");
        LocalDateTime end = DateUtils.parseDateTime(endDate, "endDate");
        return reportService.financialSummary(defaultStart(start), defaultEnd(end));
    }

    private LocalDateTime defaultStart(LocalDateTime start) {
        return start != null ? start : LocalDateTime.now().minusDays(30);
    }

    private LocalDateTime defaultEnd(LocalDateTime end) {
        return end != null ? end : LocalDateTime.now();
    }
}
