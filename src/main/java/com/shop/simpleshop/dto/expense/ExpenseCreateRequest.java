package com.shop.simpleshop.dto.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shop.simpleshop.expenses.ExpenseStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating an expense. Matches the frontend {@code ExpenseCreateRequest}.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExpenseCreateRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @NotNull(message = "Expense date is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime expenseDate;

    @Size(max = 200, message = "Vendor name must be less than 200 characters")
    private String vendor;

    private boolean receiptAttached;

    private ExpenseStatus status;
}
