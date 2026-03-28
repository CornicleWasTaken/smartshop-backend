package com.shop.simpleshop.controllers;

import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.repository.ProductRepository;
import com.shop.simpleshop.repository.SaleRepository;
import com.shop.simpleshop.sales.Sale;
import com.shop.simpleshop.sales.SaleItem;
import com.shop.simpleshop.sales.SaleStatus;
import com.shop.simpleshop.services.SaleService;
import com.shop.simpleshop.util.SaleQueryParams;
import org.junit.jupiter.api.BeforeEach;
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
class SaleControllerTest {

    @Autowired
    private SaleService saleService;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private Sale testSale;

    @BeforeEach
    void setUp() {
        // Create test product
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setSku("TEST-001");
        testProduct.setPrice(100.0);
        testProduct.setStockQuantity(50);
        testProduct.setLowStockThreshold(10);
        testProduct = productRepository.save(testProduct);

        // Create and save test sale first (without items)
        testSale = new Sale();
        testSale.setSaleDate(LocalDateTime.now());
        testSale.setCustomerPhone("1234567890");
        testSale.setStatus(SaleStatus.COMPLETED);
        testSale.setTotalAmount(BigDecimal.valueOf(200));
        testSale = saleRepository.save(testSale);

        // Create and save sale item separately
        SaleItem item = new SaleItem();
        item.setSale(testSale);
        item.setProduct(testProduct);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(100));
        item.setLineTotal(BigDecimal.valueOf(200));
        testSale.getItems().add(item);
    }

    @Test
    void getSaleById_NoParams_ReturnsFullSale() {
        var response = saleService.getSaleById(testSale.getSaleId());

        assertThat(response.getSaleId()).isEqualTo(testSale.getSaleId());
        assertThat(response.getCustomerPhone()).isEqualTo("1234567890");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.getStatus()).isEqualTo(SaleStatus.COMPLETED);
        assertThat(response.getItems()).isNotEmpty();
        assertThat(response.getItems().get(0).getProductId()).isEqualTo(testProduct.getProductId());
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("Test Product");
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void getSaleById_IncludeItemsFalse_ReturnsEmptyItems() {
        SaleQueryParams params = SaleQueryParams.parse("false", "false", null, "false");
        var response = saleService.getSaleById(testSale.getSaleId(), params);

        assertThat(response.getSaleId()).isEqualTo(testSale.getSaleId());
        assertThat(response.getItems()).isEmpty();
    }

    @Test
    void getSaleById_ExpandProductTrue_ReturnsExtendedProductFields() {
        SaleQueryParams params = SaleQueryParams.parse("true", "true", null, "false");
        var response = saleService.getSaleById(testSale.getSaleId(), params);

        assertThat(response.getItems()).isNotEmpty();
        var item = response.getItems().get(0);
        assertThat(item.getProductSku()).isEqualTo("TEST-001");
        assertThat(item.getProductPrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void getSaleById_FieldsFilter_ReturnsOnlySpecifiedFields() {
        SaleQueryParams params = SaleQueryParams.parse("true", "false", "saleId,totalAmount", "false");
        var response = saleService.getSaleById(testSale.getSaleId(), params);

        assertThat(response.getSaleId()).isEqualTo(testSale.getSaleId());
        assertThat(response.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(response.getCustomerPhone()).isNull();
        assertThat(response.getStatus()).isNull();
        assertThat(response.getSaleDate()).isNull();
    }

    @Test
    void getSaleById_NestedFieldsFilter_ReturnsOnlySpecifiedItemFields() {
        SaleQueryParams params = SaleQueryParams.parse("true", "false", "items.productName,items.quantity", "false");
        var response = saleService.getSaleById(testSale.getSaleId(), params);

        assertThat(response.getItems()).isNotEmpty();
        var item = response.getItems().get(0);
        assertThat(item.getProductName()).isEqualTo("Test Product");
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getProductId()).isNull();
        assertThat(item.getUnitPrice()).isNull();
    }

    @Test
    void getSaleById_InvalidIncludeItems_ThrowsException() {
        assertThatThrownBy(() -> SaleQueryParams.parse("maybe", "false", null, "false"))
                .isInstanceOf(com.shop.simpleshop.exceptions.InvalidQueryParameterException.class)
                .hasMessageContaining("includeItems");
    }

    @Test
    void getSaleById_InvalidExpandProduct_ThrowsException() {
        assertThatThrownBy(() -> SaleQueryParams.parse("true", "yes", null, "false"))
                .isInstanceOf(com.shop.simpleshop.exceptions.InvalidQueryParameterException.class)
                .hasMessageContaining("expandProduct");
    }

    @Test
    void getSaleById_NonExistentId_ThrowsSaleNotFoundException() {
        assertThatThrownBy(() -> saleService.getSaleById(99999L))
                .isInstanceOf(com.shop.simpleshop.exceptions.SaleNotFoundException.class);
    }

    @Test
    void getSaleById_NonExistentId_WithValidParams_ThrowsSaleNotFoundException() {
        SaleQueryParams params = SaleQueryParams.parse("false", "true", null, "false");
        assertThatThrownBy(() -> saleService.getSaleById(99999L, params))
                .isInstanceOf(com.shop.simpleshop.exceptions.SaleNotFoundException.class);
    }

    @Test
    void getSaleById_CombinationParams_WorksCorrectly() {
        SaleQueryParams params = SaleQueryParams.parse("true", "true", "saleId,items.productName,items.productSku", "false");
        var response = saleService.getSaleById(testSale.getSaleId(), params);

        assertThat(response.getSaleId()).isEqualTo(testSale.getSaleId());
        assertThat(response.getTotalAmount()).isNull();
        assertThat(response.getItems()).isNotEmpty();
        var item = response.getItems().get(0);
        assertThat(item.getProductName()).isEqualTo("Test Product");
        assertThat(item.getProductSku()).isEqualTo("TEST-001");
    }

    @Test
    void getSaleById_EmptyFieldsParam_ThrowsException() {
        assertThatThrownBy(() -> SaleQueryParams.parse("true", "false", ",,", "false"))
                .isInstanceOf(com.shop.simpleshop.exceptions.InvalidQueryParameterException.class);
    }

    @Test
    void getSaleById_PrettyTrue_WorksCorrectly() {
        SaleQueryParams params = SaleQueryParams.parse("true", "false", null, "true");
        var response = saleService.getSaleById(testSale.getSaleId(), params);

        assertThat(response.getSaleId()).isEqualTo(testSale.getSaleId());
        assertThat(params.isPretty()).isTrue();
    }
}
