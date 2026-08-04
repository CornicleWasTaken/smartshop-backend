package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.SaleItemRequestDTO;
import com.shop.simpleshop.dto.SaleRequestDTO;
import com.shop.simpleshop.dto.report.BalanceSheetData;
import com.shop.simpleshop.dto.report.FinancialSummaryData;
import com.shop.simpleshop.dto.report.ProfitLossData;
import com.shop.simpleshop.dto.report.SalesReportData;
import com.shop.simpleshop.entity.Expense;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.expenses.ExpenseStatus;
import com.shop.simpleshop.repository.ExpenseRepository;
import com.shop.simpleshop.repository.InventoryTransactionRepository;
import com.shop.simpleshop.repository.ProductRepository;
import com.shop.simpleshop.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private EntityManager entityManager;

    /**
     * Reports aggregate across the whole database, so these tests need a clean slate.
     * All deletes happen inside the test transaction and are rolled back afterwards.
     */
    @BeforeEach
    void cleanReportData() {
        inventoryTransactionRepository.deleteAll();
        expenseRepository.deleteAll();
        saleRepository.deleteAll(); // cascades sale items
        productRepository.deleteAll();
    }

    private Product createProduct(String name, String sku, double price, int stock, String category) {
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setLowStockThreshold(5);
        product.setCategory(category);
        return productRepository.save(product);
    }

    private void createSale(Long productId, int quantity) {
        SaleItemRequestDTO item = new SaleItemRequestDTO();
        item.setProductId(productId);
        item.setQuantity(quantity);

        SaleRequestDTO request = new SaleRequestDTO();
        request.setCustomerPhone("1234567890");
        request.setItems(List.of(item));

        saleService.createSale(request);
    }

    private void saveExpense(String description, String amount, ExpenseStatus status) {
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(new BigDecimal(amount));
        expense.setCategory("Rent");
        expense.setExpenseDate(LocalDateTime.now());
        expense.setReceiptAttached(false);
        expense.setStatus(status);
        expenseRepository.save(expense);
    }

    private LocalDateTime start() {
        return LocalDateTime.now().minusDays(1);
    }

    private LocalDateTime end() {
        return LocalDateTime.now().plusDays(1);
    }

    @Test
    void salesReport_aggregatesTotalsAndCategories() {
        Product electronics = createProduct("Widget", "W-1", 10.00, 100, "Electronics");
        createProduct("Gadget", "G-1", 5.00, 100, "Uncategorized");
        createSale(electronics.getProductId(), 2);
        createSale(electronics.getProductId(), 3);

        SalesReportData report = reportService.salesReport(start(), end());

        assertThat(report.getTotalTransactions()).isEqualTo(2);
        assertThat(report.getTotalSales()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(report.getTotalItemsSold()).isEqualTo(5);
        assertThat(report.getDailyBreakdown()).isNotEmpty();
        assertThat(report.getTopProducts()).isNotEmpty();
        assertThat(report.getTopProducts().get(0).getTotalRevenue()).isEqualByComparingTo(new BigDecimal("50.00"));

        var electronicsCategory = report.getCategoryBreakdown().stream()
                .filter(c -> c.getCategory().equals("Electronics"))
                .findFirst()
                .orElseThrow();
        assertThat(electronicsCategory.getItemsSold()).isEqualTo(5);
    }

    @Test
    void profitLoss_usesRecognizedExpensesOnly() {
        Product product = createProduct("Widget", "W-1", 10.00, 100, "Electronics");
        createSale(product.getProductId(), 2); // 20.00 sales

        saveExpense("Rent", "100.00", ExpenseStatus.PAID);      // recognized
        saveExpense("Draft", "50.00", ExpenseStatus.PENDING);    // not recognized

        ProfitLossData pl = reportService.profitLoss(start(), end());

        assertThat(pl.getTotalSales()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(pl.getSalesReturns()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(pl.getNetSales()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(pl.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(pl.getGrossProfit()).isEqualByComparingTo(new BigDecimal("-80.00"));
        assertThat(pl.getNetProfit()).isEqualByComparingTo(new BigDecimal("-80.00"));
    }

    @Test
    void balanceSheet_balancesByConstruction() {
        Product product = createProduct("Widget", "W-1", 10.00, 100, "Electronics");
        createSale(product.getProductId(), 2); // +20 lifetime sales
        saveExpense("Rent", "100.00", ExpenseStatus.PAID); // -100 recognized

        // balanceSheet runs in a read-only transaction, so flush pending inserts
        // first — otherwise the un-flushed product is invisible to findAll() here.
        entityManager.flush();

        BalanceSheetData bs = reportService.balanceSheet(LocalDate.now());

        // The sale of 2 units decremented stock from 100 -> 98, so inventory = 98 x 10.
        assertThat(bs.getInventoryValue()).isEqualByComparingTo(new BigDecimal("980.00"));
        assertThat(bs.getCash()).isEqualByComparingTo(new BigDecimal("-80.00")); // 20 - 100
        assertThat(bs.getTotalAssets()).isEqualByComparingTo(new BigDecimal("900.00")); // cash + inventory
        assertThat(bs.getEquity()).isEqualByComparingTo(new BigDecimal("900.00")); // retained earnings + inventory
        assertThat(bs.getTotalLiabilitiesAndEquity()).isEqualByComparingTo(bs.getTotalAssets());
        assertThat(bs.isBalanceSheetBalanced()).isTrue();
    }

    @Test
    void financialSummary_aggregatesRange() {
        Product product = createProduct("Widget", "W-1", 10.00, 100, "Electronics");
        createSale(product.getProductId(), 2); // 20 sales
        saveExpense("Rent", "5.00", ExpenseStatus.APPROVED); // recognized

        FinancialSummaryData summary = reportService.financialSummary(start(), end());

        assertThat(summary.getTotalSales()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(summary.getGrossProfit()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(summary.getNetProfit()).isEqualByComparingTo(new BigDecimal("15.00"));
    }
}
