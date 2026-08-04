package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.expense.ExpenseCreateRequest;
import com.shop.simpleshop.dto.expense.ExpenseData;
import com.shop.simpleshop.dto.expense.ExpenseReportData;
import com.shop.simpleshop.exceptions.ExpenseNotFoundException;
import com.shop.simpleshop.expenses.ExpenseStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    private ExpenseCreateRequest request(String description, String amount, ExpenseStatus status) {
        ExpenseCreateRequest request = new ExpenseCreateRequest();
        request.setDescription(description);
        request.setAmount(new BigDecimal(amount));
        request.setCategory("Rent");
        request.setExpenseDate(LocalDateTime.now());
        request.setReceiptAttached(false);
        request.setStatus(status);
        return request;
    }

    @Test
    void createAndFetch_roundTrips() {
        ExpenseData created = expenseService.create(request("Monthly rent", "1000.00", ExpenseStatus.PAID));

        assertThat(created.getExpenseId()).isNotNull();
        ExpenseData fetched = expenseService.getById(created.getExpenseId());
        assertThat(fetched.getDescription()).isEqualTo("Monthly rent");
        assertThat(fetched.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(fetched.getCreatedDate()).isNotNull();
        assertThat(fetched.getUpdatedDate()).isNotNull();
    }

    @Test
    void create_defaultsStatusToDraftWhenNull() {
        ExpenseCreateRequest request = request("Misc", "25.00", null);
        ExpenseData created = expenseService.create(request);
        assertThat(created.getStatus()).isEqualTo(ExpenseStatus.DRAFT);
    }

    @Test
    void updateAndDelete_work() {
        ExpenseData created = expenseService.create(request("Old", "10.00", ExpenseStatus.PENDING));
        ExpenseCreateRequest update = request("New", "20.00", ExpenseStatus.APPROVED);
        update.setCategory("Utilities");

        ExpenseData updated = expenseService.update(created.getExpenseId(), update);
        assertThat(updated.getDescription()).isEqualTo("New");
        assertThat(updated.getStatus()).isEqualTo(ExpenseStatus.APPROVED);

        expenseService.delete(created.getExpenseId());
        assertThatThrownBy(() -> expenseService.getById(created.getExpenseId()))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void generateReport_aggregatesTotalsCategoriesAndStatus() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        expenseService.create(request("Rent A", "1000.00", ExpenseStatus.PAID));
        expenseService.create(request("Rent B", "500.00", ExpenseStatus.APPROVED));
        expenseService.create(request("Utilities", "300.00", ExpenseStatus.PENDING));

        ExpenseReportData report = expenseService.generateReport(start, end);

        assertThat(report.getTotalItems()).isEqualTo(3);
        assertThat(report.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("1800.00"));
        assertThat(report.getAverageAmount()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(report.getLargestExpense()).isEqualByComparingTo(new BigDecimal("1000.00"));

        // Status summary excludes DRAFT but includes the other statuses.
        assertThat(report.getStatusSummary().getPaidCount()).isEqualTo(1);
        assertThat(report.getStatusSummary().getApprovedCount()).isEqualTo(1);
        assertThat(report.getStatusSummary().getPendingCount()).isEqualTo(1);
        assertThat(report.getStatusSummary().getPaidAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));

        // Top expenses sorted by amount desc.
        assertThat(report.getTopExpenses()).hasSize(3);
        assertThat(report.getTopExpenses().get(0).getDescription()).isEqualTo("Rent A");
    }
}
