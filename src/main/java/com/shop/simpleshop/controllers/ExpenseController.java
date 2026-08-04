package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.PageResponse;
import com.shop.simpleshop.dto.expense.ExpenseCreateRequest;
import com.shop.simpleshop.dto.expense.ExpenseData;
import com.shop.simpleshop.dto.expense.ExpenseReportData;
import com.shop.simpleshop.expenses.ExpenseStatus;
import com.shop.simpleshop.services.ExpenseService;
import com.shop.simpleshop.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/expenses")
@Tag(name = "Expenses", description = "Endpoints for managing business expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    @Operation(summary = "List expenses with optional filters")
    public PageResponse<ExpenseData> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ExpenseStatus status) {

        LocalDateTime start = DateUtils.parseDateTime(startDate, "startDate");
        LocalDateTime end = DateUtils.parseDateTime(endDate, "endDate");
        return expenseService.list(page, size, start, end, category, status);
    }

    @GetMapping("/date-range")
    @Operation(summary = "List expenses within a date range")
    public PageResponse<ExpenseData> byDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return expenseService.byDateRange(page, size,
                DateUtils.parseDateTime(startDate, "startDate"),
                DateUtils.parseDateTime(endDate, "endDate"));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "List expenses for a category")
    public PageResponse<ExpenseData> byCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return expenseService.byCategory(page, size, category);
    }

    @GetMapping("/reports")
    @Operation(summary = "Generate an expense report for a date range")
    public ExpenseReportData generateReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start = DateUtils.parseDateTime(startDate, "startDate");
        LocalDateTime end = DateUtils.parseDateTime(endDate, "endDate");
        if (start == null) {
            start = LocalDateTime.now().minusDays(30);
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        return expenseService.generateReport(start, end);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expense by ID")
    public ExpenseData getById(@PathVariable Long id) {
        return expenseService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseData> create(@Valid @RequestBody ExpenseCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense")
    public ExpenseData update(@PathVariable Long id, @Valid @RequestBody ExpenseCreateRequest request) {
        return expenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
