package com.shop.simpleshop.security;

import com.shop.simpleshop.controllers.ExpenseController;
import com.shop.simpleshop.controllers.ProductController;
import com.shop.simpleshop.controllers.SaleController;
import com.shop.simpleshop.controllers.UserController;
import com.shop.simpleshop.entity.Product;
import com.shop.simpleshop.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the {@code @PreAuthorize} matrix. Method-security proxies enforce the
 * guards on the controller beans even when called directly, so we authenticate a
 * synthetic principal via {@link SecurityContextHolder} and assert the denial /
 * grant behavior at the bean boundary.
 *
 * Requires Oracle (DB-backed), consistent with the other @SpringBootTest suites.
 */
@SpringBootTest
@Transactional
class RoleEnforcementTest {

    @Autowired
    private ProductController productController;
    @Autowired
    private ExpenseController expenseController;
    @Autowired
    private SaleController saleController;
    @Autowired
    private UserController userController;
    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String username, String role, boolean override) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        if (override) {
            authorities.add(new SimpleGrantedAuthority("OVERRIDE"));
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, authorities));
    }

    private Product newProduct(String suffix) {
        Product product = new Product();
        product.setName("Enforcement " + suffix);
        product.setSku("ENF-" + suffix);
        product.setPrice(10.0);
        product.setStockQuantity(10);
        product.setLowStockThreshold(5);
        return productRepository.save(product);
    }

    @Test
    void cashier_cannotDeleteProduct() {
        authenticateAs("cashier1", "CASHIER", false);
        assertThatThrownBy(() -> productController.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cashier_cannotDeleteExpense() {
        authenticateAs("cashier1", "CASHIER", false);
        assertThatThrownBy(() -> expenseController.delete(1L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cashier_cannotVoidSale() {
        authenticateAs("cashier1", "CASHIER", false);
        assertThatThrownBy(() -> saleController.voidSale(1L, null))
                .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> saleController.voidItem(1L, 1L, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cashier_cannotListOrChangeUsers() {
        authenticateAs("cashier1", "CASHIER", false);
        assertThatThrownBy(() -> userController.listUsers())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cashier_withOverrideAuthority_canDeleteProduct() {
        authenticateAs("cashier1", "CASHIER", true);
        Product product = newProduct(String.valueOf(System.nanoTime()));
        // Guard passes; if it did not, AccessDeniedException would surface before the body.
        assertThatCode(() -> productController.delete(product.getProductId()))
                .doesNotThrowAnyException();
    }

    @Test
    void manager_canDeleteProduct() {
        authenticateAs("manager1", "MANAGER", false);
        Product product = newProduct(String.valueOf(System.nanoTime()));
        assertThatCode(() -> productController.delete(product.getProductId()))
                .doesNotThrowAnyException();
    }

    @Test
    void admin_canListUsers() {
        authenticateAs("admin1", "ADMIN", false);
        assertThatCode(() -> userController.listUsers())
                .doesNotThrowAnyException();
    }

    @Test
    void cashier_canFetchProducts_readsAreNotRestricted() {
        authenticateAs("cashier1", "CASHIER", false);
        assertThatCode(() -> productController.getAll(org.springframework.data.domain.PageRequest.of(0, 1)))
                .doesNotThrowAnyException();
    }
}