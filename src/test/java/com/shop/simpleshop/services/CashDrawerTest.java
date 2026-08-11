package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.drawer.CashDrawerSessionResponse;
import com.shop.simpleshop.exceptions.DrawerAlreadyOpenException;
import com.shop.simpleshop.pos.DrawerStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the cash-drawer lifecycle: opening, closing with variance, the
 * single-open rule, and paginated session history.
 *
 * Requires Oracle (DB-backed).
 */
@SpringBootTest
@Transactional
class CashDrawerTest {

    @Autowired
    private CashDrawerService drawerService;

    private final AtomicLong seq = new AtomicLong(1);

    @Test
    void openThenClose_recordsVariance() {
        CashDrawerSessionResponse opened = drawerService.open("cashier" + seq.getAndIncrement(), new BigDecimal("100.00"));

        assertThat(opened.drawerId()).isNotNull();
        assertThat(opened.status()).isEqualTo(DrawerStatus.OPEN);
        assertThat(opened.openedAt()).isNotNull();
        assertThat(opened.closedAt()).isNull();

        CashDrawerSessionResponse closed = drawerService.close(opened.drawerId(), new BigDecimal("125.00"));

        assertThat(closed.status()).isEqualTo(DrawerStatus.CLOSED);
        assertThat(closed.closedAt()).isNotNull();
        assertThat(closed.variance()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void open_secondWhileOpen_throws() {
        CashDrawerSessionResponse opened = drawerService.open("cashierA", new BigDecimal("100.00"));

        assertThatThrownBy(() -> drawerService.open("cashierB", new BigDecimal("50.00")))
                .isInstanceOf(DrawerAlreadyOpenException.class);

        drawerService.close(opened.drawerId(), new BigDecimal("110.00"));
    }

    @Test
    void close_missingSession_throws() {
        assertThatThrownBy(() -> drawerService.close(99999L, new BigDecimal("0.00")))
                .isInstanceOf(DrawerAlreadyOpenException.class);
    }

    @Test
    void sessions_listsClosedHistory() {
        CashDrawerSessionResponse opened = drawerService.open("cashierC", new BigDecimal("100.00"));
        drawerService.close(opened.drawerId(), new BigDecimal("120.00"));

        Page<CashDrawerSessionResponse> page = drawerService.sessions(PageRequest.of(0, 20));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().get(0).drawerId()).isEqualTo(opened.drawerId());
        assertThat(page.getContent().get(0).status()).isEqualTo(DrawerStatus.CLOSED);
    }
}