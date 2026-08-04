package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.report.BalanceSheetData;
import com.shop.simpleshop.dto.report.CategorySalesData;
import com.shop.simpleshop.dto.report.DailySalesData;
import com.shop.simpleshop.dto.report.FinancialSummaryData;
import com.shop.simpleshop.dto.report.ProfitLossData;
import com.shop.simpleshop.dto.report.ProductSalesData;
import com.shop.simpleshop.dto.report.SalesReportData;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.repository.ExpenseRepository;
import com.shop.simpleshop.repository.ProductRepository;
import com.shop.simpleshop.repository.SaleRepository;
import com.shop.simpleshop.sales.Sale;
import com.shop.simpleshop.sales.SaleItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates sales, expense, and inventory data into financial reports.
 *
 * Documented approximations (see README):
 * - No product cost / COGS model: gross profit = net sales - recognized expenses.
 * - No returns model: salesReturns is always 0; non-COMPLETED sales are excluded from totals.
 * - No cash/AP/equity ledgers: the balance sheet is synthesized. cash == retainedEarnings
 *   (lifetime sales minus lifetime recognized expenses); equity = retainedEarnings +
 *   inventoryValue (inventory financed by owner equity), so the sheet balances.
 */
@Service
public class ReportService {

    private static final int TOP_PRODUCTS_LIMIT = 10;

    private final SaleRepository saleRepository;
    private final ExpenseRepository expenseRepository;
    private final ProductRepository productRepository;

    public ReportService(SaleRepository saleRepository,
                         ExpenseRepository expenseRepository,
                         ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.expenseRepository = expenseRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public SalesReportData salesReport(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = saleRepository.findCompletedWithItemsBetween(start, end);

        SalesReportData report = new SalesReportData();
        report.setPeriodStart(start);
        report.setPeriodEnd(end);

        BigDecimal totalSales = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalTransactions = sales.size();
        long totalItemsSold = sales.stream()
                .flatMap(s -> s.getItems().stream())
                .mapToLong(SaleItem::getQuantity)
                .sum();

        report.setTotalSales(totalSales);
        report.setTotalTransactions(totalTransactions);
        report.setAverageTransactionValue(totalTransactions > 0
                ? totalSales.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        report.setTotalItemsSold(totalItemsSold);
        report.setDailyBreakdown(buildDailyBreakdown(sales));
        report.setTopProducts(buildTopProducts(sales, totalSales));
        report.setCategoryBreakdown(buildSalesCategoryBreakdown(sales, totalSales));

        return report;
    }

    @Transactional(readOnly = true)
    public ProfitLossData profitLoss(LocalDateTime start, LocalDateTime end) {
        BigDecimal totalSales = saleRepository.findCompletedWithItemsBetween(start, end).stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseRepository.sumRecognizedBetween(start, end);

        // Approximations (no COGS / returns model): see class javadoc.
        BigDecimal salesReturns = BigDecimal.ZERO;
        BigDecimal netSales = totalSales;
        BigDecimal operatingExpenses = totalExpenses;
        BigDecimal grossProfit = netSales.subtract(totalExpenses);
        BigDecimal operatingProfit = grossProfit;
        BigDecimal netProfit = grossProfit;
        BigDecimal grossMargin = percentage(netSales, grossProfit);
        BigDecimal netMargin = percentage(netSales, netProfit);

        ProfitLossData data = new ProfitLossData();
        data.setPeriodStart(start);
        data.setPeriodEnd(end);
        data.setTotalSales(totalSales);
        data.setSalesReturns(salesReturns);
        data.setNetSales(netSales);
        data.setTotalExpenses(totalExpenses);
        data.setOperatingExpenses(operatingExpenses);
        data.setGrossProfit(grossProfit);
        data.setOperatingProfit(operatingProfit);
        data.setNetProfit(netProfit);
        data.setGrossMarginPercentage(grossMargin);
        data.setNetMarginPercentage(netMargin);
        return data;
    }

    @Transactional(readOnly = true)
    public BalanceSheetData balanceSheet(LocalDate asOfDate) {
        List<Product> products = productRepository.findAll();
        BigDecimal inventoryValue = products.stream()
                .map(p -> BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal lifetimeSales = saleRepository.sumLifetimeCompleted();
        BigDecimal lifetimeExpenses = expenseRepository.sumRecognizedLifetime();
        BigDecimal cash = lifetimeSales.subtract(lifetimeExpenses);
        BigDecimal retainedEarnings = cash;

        BigDecimal currentAssets = cash.add(inventoryValue);
        BigDecimal totalAssets = currentAssets;
        BigDecimal currentLiabilities = BigDecimal.ZERO;
        BigDecimal accountsPayable = BigDecimal.ZERO;
        BigDecimal totalLiabilities = currentLiabilities.add(accountsPayable);
        // Inventory represents capital tied up in stock, financed by owner equity.
        // Adding it to retained earnings keeps the sheet balanced by construction.
        BigDecimal equity = retainedEarnings.add(inventoryValue);
        BigDecimal totalEquity = equity;
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);

        BalanceSheetData data = new BalanceSheetData();
        data.setAsOfDate(asOfDate);
        data.setInventoryValue(inventoryValue);
        data.setCash(cash);
        data.setCurrentAssets(currentAssets);
        data.setTotalAssets(totalAssets);
        data.setCurrentLiabilities(currentLiabilities);
        data.setAccountsPayable(accountsPayable);
        data.setTotalLiabilities(totalLiabilities);
        data.setEquity(equity);
        data.setRetainedEarnings(retainedEarnings);
        data.setTotalEquity(totalEquity);
        data.setTotalLiabilitiesAndEquity(totalLiabilitiesAndEquity);
        data.setBalanceSheetBalanced(totalAssets.compareTo(totalLiabilitiesAndEquity) == 0);
        return data;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryData financialSummary(LocalDateTime start, LocalDateTime end) {
        BigDecimal totalSales = saleRepository.findCompletedWithItemsBetween(start, end).stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseRepository.sumRecognizedBetween(start, end);
        BigDecimal grossProfit = totalSales.subtract(totalExpenses);

        FinancialSummaryData data = new FinancialSummaryData();
        data.setPeriodStart(start);
        data.setPeriodEnd(end);
        data.setTotalSales(totalSales);
        data.setTotalExpenses(totalExpenses);
        data.setGrossProfit(grossProfit);
        data.setNetProfit(grossProfit);
        return data;
    }

    private List<DailySalesData> buildDailyBreakdown(List<Sale> sales) {
        Map<LocalDate, DailyAccumulator> byDay = new LinkedHashMap<>();
        for (Sale sale : sales) {
            LocalDate day = sale.getSaleDate().toLocalDate();
            byDay.computeIfAbsent(day, k -> new DailyAccumulator())
                    .add(sale.getTotalAmount(), sale.getItems().stream().mapToLong(SaleItem::getQuantity).sum());
        }
        List<DailySalesData> breakdown = new ArrayList<>();
        for (Map.Entry<LocalDate, DailyAccumulator> entry : byDay.entrySet()) {
            DailyAccumulator acc = entry.getValue();
            BigDecimal avg = acc.transactions > 0
                    ? acc.amount.divide(BigDecimal.valueOf(acc.transactions), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            breakdown.add(new DailySalesData(entry.getKey(), acc.transactions, acc.amount, avg));
        }
        return breakdown;
    }

    private List<ProductSalesData> buildTopProducts(List<Sale> sales, BigDecimal totalSales) {
        Map<Long, ProductAccumulator> byProduct = new LinkedHashMap<>();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                Long productId = item.getProduct().getProductId();
                byProduct.computeIfAbsent(productId, k -> new ProductAccumulator(item.getProduct().getName()))
                        .add(item.getQuantity(), item.getLineTotal());
            }
        }
        return byProduct.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().revenue.compareTo(e1.getValue().revenue))
                .limit(TOP_PRODUCTS_LIMIT)
                .map(e -> new ProductSalesData(e.getKey(), e.getValue().name, e.getValue().quantitySold,
                        e.getValue().revenue, percentageOf(totalSales, e.getValue().revenue)))
                .toList();
    }

    private List<CategorySalesData> buildSalesCategoryBreakdown(List<Sale> sales, BigDecimal totalSales) {
        Map<String, CategoryAccumulator> byCategory = new LinkedHashMap<>();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getItems()) {
                String category = item.getProduct().getCategory() == null
                        ? "Uncategorized"
                        : item.getProduct().getCategory();
                byCategory.computeIfAbsent(category, k -> new CategoryAccumulator())
                        .add(item.getQuantity(), item.getLineTotal());
            }
        }
        return byCategory.entrySet().stream()
                .map(entry -> new CategorySalesData(entry.getKey(), entry.getValue().itemsSold,
                        entry.getValue().revenue, percentageOf(totalSales, entry.getValue().revenue)))
                .sorted(Comparator.comparing(CategorySalesData::getRevenue).reversed())
                .toList();
    }

    private BigDecimal percentage(BigDecimal base, BigDecimal value) {
        return base.signum() > 0
                ? value.multiply(BigDecimal.valueOf(100)).divide(base, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
    }

    private double percentageOf(BigDecimal base, BigDecimal value) {
        return percentage(base, value).doubleValue();
    }

    private static class DailyAccumulator {
        long transactions = 0;
        BigDecimal amount = BigDecimal.ZERO;

        void add(BigDecimal value, long itemCount) {
            transactions++;
            amount = amount.add(value);
        }
    }

    private static class ProductAccumulator {
        final String name;
        long quantitySold = 0;
        BigDecimal revenue = BigDecimal.ZERO;

        ProductAccumulator(String name) {
            this.name = name;
        }

        void add(long quantity, BigDecimal lineTotal) {
            quantitySold += quantity;
            revenue = revenue.add(lineTotal);
        }
    }

    private static class CategoryAccumulator {
        long itemsSold = 0;
        BigDecimal revenue = BigDecimal.ZERO;

        void add(long quantity, BigDecimal lineTotal) {
            itemsSold += quantity;
            revenue = revenue.add(lineTotal);
        }
    }
}
