package com.shop.simpleshop.services;

import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.exceptions.ForbiddenActionException;
import com.shop.simpleshop.exceptions.SaleItemNotFoundException;
import com.shop.simpleshop.repository.ProductRepository;
import com.shop.simpleshop.repository.SaleRepository;
import com.shop.simpleshop.repository.TransactionVoidRepository;
import com.shop.simpleshop.sales.Sale;
import com.shop.simpleshop.sales.SaleItem;
import com.shop.simpleshop.sales.SaleStatus;
import com.shop.simpleshop.sales.TransactionVoid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies line-item and whole-sale voids: stock reversal, total recompute,
 * and the persisted {@link TransactionVoid} records.
 *
 * Requires Oracle (DB-backed).
 */
@SpringBootTest
@Transactional
class VoidServiceTest {

    @Autowired
    private SaleService saleService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SaleRepository saleRepository;
    @Autowired
    private TransactionVoidRepository transactionVoidRepository;

    private Product product(int stock) {
        Product product = new Product();
        product.setName("Void Product " + System.nanoTime());
        product.setSku("VOID-" + System.nanoTime());
        product.setPrice(100.0);
        product.setStockQuantity(stock);
        product.setLowStockThreshold(5);
        return productRepository.save(product);
    }

    private Sale completedSale(Product product, int quantity) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(SaleStatus.COMPLETED);
        BigDecimal lineTotal = BigDecimal.valueOf(product.getPrice()).multiply(BigDecimal.valueOf(quantity));
        sale.setTotalAmount(lineTotal);

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
        item.setLineTotal(lineTotal);
        sale.addItem(item);
        return saleRepository.save(sale);
    }

    @Test
    void voidItem_reversesStockDropsTotalAndRecordsVoid() {
        Product product = product(50);
        Sale sale = completedSale(product, 2);
        SaleItem item = sale.getItems().get(0);

        var response = saleService.voidItem(sale.getSaleId(), item.getSaleItemId(), "customer changed mind");

        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(productRepository.findById(product.getProductId()).orElseThrow().getStockQuantity()).isEqualTo(52);

        List<TransactionVoid> voids = transactionVoidRepository.findAll();
        TransactionVoid record = voids.stream()
                .filter(v -> v.getSale().getSaleId().equals(sale.getSaleId()))
                .findFirst().orElseThrow();
        assertThat(record.getSaleItem()).isNotNull();
        assertThat(record.getSale().getSaleId()).isEqualTo(sale.getSaleId());
    }

    @Test
    void voidSale_cancelsRestoresAllStockAndRecordsSingleVoid() {
        Product product = product(50);
        Sale sale = completedSale(product, 3);

        var response = saleService.voidSale(sale.getSaleId(), "test order");

        assertThat(response.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(productRepository.findById(product.getProductId()).orElseThrow().getStockQuantity()).isEqualTo(53);

        List<TransactionVoid> voids = transactionVoidRepository.findAll();
        TransactionVoid record = voids.stream()
                .filter(v -> v.getSale().getSaleId().equals(sale.getSaleId()))
                .findFirst().orElseThrow();
        assertThat(record.getSaleItem()).isNull();
    }

    @Test
    void voidSale_onlyCompletedSalesAreVoidable() {
        Product product = product(5);
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setStatus(SaleStatus.PENDING);
        sale.setTotalAmount(BigDecimal.valueOf(100));
        Sale pending = saleRepository.save(sale);

        assertThatThrownBy(() -> saleService.voidSale(pending.getSaleId(), "nope"))
                .isInstanceOf(ForbiddenActionException.class);
    }

    @Test
    void voidItem_itemOutsideSale_throws() {
        Product product = product(10);
        Sale sale = completedSale(product, 1);

        assertThatThrownBy(() -> saleService.voidItem(sale.getSaleId(), 99999L, "wrong item"))
                .isInstanceOf(SaleItemNotFoundException.class);
    }
}