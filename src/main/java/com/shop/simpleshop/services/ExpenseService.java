package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.PageResponse;
import com.shop.simpleshop.dto.expense.CategoryExpenseData;
import com.shop.simpleshop.dto.expense.ExpenseCreateRequest;
import com.shop.simpleshop.dto.expense.ExpenseData;
import com.shop.simpleshop.dto.expense.ExpenseReportData;
import com.shop.simpleshop.dto.expense.ExpenseStatusSummaryData;
import com.shop.simpleshop.entity.Expense;
import com.shop.simpleshop.exceptions.ExpenseNotFoundException;
import com.shop.simpleshop.expenses.ExpenseStatus;
import com.shop.simpleshop.repository.ExpenseRepository;
import com.shop.simpleshop.repository.ExpenseSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for the expenses module: CRUD, date-range/category listing,
 * and the aggregate expense report.
 */
@Service
public class ExpenseService {

    private static final int TOP_EXPENSES_LIMIT = 10;

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseData> list(int page, int size, LocalDateTime startDate, LocalDateTime endDate,
                                          String category, ExpenseStatus status) {
        Pageable pageable = defaultSort(PageRequest.of(page, size));
        Page<Expense> result = expenseRepository.findAll(
                ExpenseSpecifications.build(startDate, endDate, category, status), pageable);
        return PageResponse.from(result.map(this::toExpenseData));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseData> byDateRange(int page, int size, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = defaultSort(PageRequest.of(page, size));
        Page<Expense> result = expenseRepository.findAll(
                ExpenseSpecifications.build(startDate, endDate, null, null), pageable);
        return PageResponse.from(result.map(this::toExpenseData));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseData> byCategory(int page, int size, String category) {
        Pageable pageable = defaultSort(PageRequest.of(page, size));
        Page<Expense> result = expenseRepository.findAll(
                ExpenseSpecifications.build(null, null, category, null), pageable);
        return PageResponse.from(result.map(this::toExpenseData));
    }

    @Transactional(readOnly = true)
    public ExpenseData getById(Long id) {
        return toExpenseData(findOrThrow(id));
    }

    @Transactional
    public ExpenseData create(ExpenseCreateRequest request) {
        Expense expense = new Expense();
        applyRequest(expense, request);
        if (expense.getStatus() == null) {
            expense.setStatus(ExpenseStatus.DRAFT);
        }
        return toExpenseData(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseData update(Long id, ExpenseCreateRequest request) {
        Expense expense = findOrThrow(id);
        applyRequest(expense, request);
        return toExpenseData(expenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = findOrThrow(id);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseReportData generateReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Expense> inRange = expenseRepository.findAll(
                ExpenseSpecifications.build(startDate, endDate, null, null));

        ExpenseReportData report = new ExpenseReportData();
        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);

        long totalItems = inRange.size();
        report.setTotalItems(totalItems);

        BigDecimal total = inRange.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.setTotalExpenses(total);
        report.setAverageAmount(totalItems > 0
                ? total.divide(BigDecimal.valueOf(totalItems), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
        report.setLargestExpense(inRange.stream()
                .map(Expense::getAmount)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO));

        report.setCategoryBreakdown(buildCategoryBreakdown(inRange, total));
        report.setTopExpenses(buildTopExpenses(inRange));
        report.setStatusSummary(buildStatusSummary(inRange));

        return report;
    }

    private Expense findOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    private void applyRequest(Expense expense, ExpenseCreateRequest request) {
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setVendor(request.getVendor());
        expense.setReceiptAttached(request.isReceiptAttached());
        expense.setStatus(request.getStatus());
    }

    private List<CategoryExpenseData> buildCategoryBreakdown(List<Expense> expenses, BigDecimal total) {
        Map<String, CategoryAccumulator> byCategory = new LinkedHashMap<>();
        for (Expense expense : expenses) {
            String category = expense.getCategory() == null ? "Uncategorized" : expense.getCategory();
            byCategory.computeIfAbsent(category, k -> new CategoryAccumulator())
                    .add(expense.getAmount());
        }
        List<CategoryExpenseData> breakdown = new ArrayList<>();
        for (Map.Entry<String, CategoryAccumulator> entry : byCategory.entrySet()) {
            CategoryAccumulator acc = entry.getValue();
            double percentage = total.signum() > 0
                    ? acc.amount.divide(total, 4, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0;
            breakdown.add(new CategoryExpenseData(entry.getKey(), acc.amount, acc.items, percentage));
        }
        breakdown.sort(Comparator.comparing(CategoryExpenseData::getAmount).reversed());
        return breakdown;
    }

    private List<ExpenseData> buildTopExpenses(List<Expense> expenses) {
        return expenses.stream()
                .sorted(Comparator.comparing(Expense::getAmount).reversed())
                .limit(TOP_EXPENSES_LIMIT)
                .map(this::toExpenseData)
                .toList();
    }

    private ExpenseStatusSummaryData buildStatusSummary(List<Expense> expenses) {
        long pendingCount = 0, approvedCount = 0, paidCount = 0, rejectedCount = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO, approvedAmount = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO, rejectedAmount = BigDecimal.ZERO;

        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount();
            switch (expense.getStatus()) {
                case PENDING -> { pendingCount++; pendingAmount = pendingAmount.add(amount); }
                case APPROVED -> { approvedCount++; approvedAmount = approvedAmount.add(amount); }
                case PAID -> { paidCount++; paidAmount = paidAmount.add(amount); }
                case REJECTED -> { rejectedCount++; rejectedAmount = rejectedAmount.add(amount); }
                default -> { /* DRAFT intentionally excluded to match the frontend */ }
            }
        }
        return new ExpenseStatusSummaryData(pendingCount, pendingAmount, approvedCount, approvedAmount,
                paidCount, paidAmount, rejectedCount, rejectedAmount);
    }

    private ExpenseData toExpenseData(Expense expense) {
        ExpenseData data = new ExpenseData();
        data.setExpenseId(expense.getExpenseId());
        data.setDescription(expense.getDescription());
        data.setAmount(expense.getAmount());
        data.setCategory(expense.getCategory());
        data.setExpenseDate(expense.getExpenseDate());
        data.setVendor(expense.getVendor());
        data.setReceiptAttached(expense.isReceiptAttached());
        data.setStatus(expense.getStatus());
        data.setCreatedDate(expense.getCreatedDate());
        data.setUpdatedDate(expense.getUpdatedDate());
        return data;
    }

    private Pageable defaultSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "expenseDate"));
        }
        return pageable;
    }

    private static class CategoryAccumulator {
        BigDecimal amount = BigDecimal.ZERO;
        long items = 0;

        void add(BigDecimal value) {
            amount = amount.add(value);
            items++;
        }
    }
}
