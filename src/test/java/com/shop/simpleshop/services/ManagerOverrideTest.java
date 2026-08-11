package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.auth.VerifyManagerRequest;
import com.shop.simpleshop.dto.auth.VerifyManagerResponseDTO;
import com.shop.simpleshop.entity.User;
import com.shop.simpleshop.exceptions.InsufficientRoleException;
import com.shop.simpleshop.exceptions.InvalidCredentialsException;
import com.shop.simpleshop.repository.UserRepository;
import com.shop.simpleshop.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises POST /api/auth/verify-manager via the service bean. The role/override
 * authority in the security context is not what the endpoint trusts: it re-reads
 * the DB user, so these tests set the context for realism only.
 *
 * Requires Oracle (DB-backed).
 */
@SpringBootTest
@Transactional
class ManagerOverrideTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User user(String username, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPasswordHash(passwordEncoder.encode("Pass123!"));
        user.setRole(role);
        return userRepository.save(user);
    }

    private void authenticateAs(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private VerifyManagerRequest password(String value) {
        VerifyManagerRequest request = new VerifyManagerRequest();
        request.setPassword(value);
        return request;
    }

    @Test
    void cashier_verifyManager_failsWithInsufficientRole() {
        user("cashie1", "CASHIER");
        authenticateAs("cashie1", "CASHIER");

        assertThatThrownBy(() -> authService.verifyManager("cashie1", password("Pass123!")))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void manager_verifyManager_returnsOverrideToken() {
        user("manage1", "MANAGER");
        authenticateAs("manage1", "MANAGER");

        VerifyManagerResponseDTO response = authService.verifyManager("manage1", password("Pass123!"));

        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(jwtService.parseUserId(response.getAccessToken())).isNotNull();
        assertThat(jwtService.parseRole(response.getAccessToken())).isEqualTo("MANAGER");
        assertThat(jwtService.parseOverrideBy(response.getAccessToken())).isEqualTo("manage1");
        assertThat(jwtService.parseOverrideExpiry(response.getAccessToken())).isNotNull();
    }

    @Test
    void admin_verifyManager_returnsOverrideToken() {
        user("admin1", "ADMIN");
        authenticateAs("admin1", "ADMIN");

        VerifyManagerResponseDTO response = authService.verifyManager("admin1", password("Pass123!"));
        assertThat(jwtService.parseOverrideBy(response.getAccessToken())).isEqualTo("admin1");
        assertThat(jwtService.parseOverrideExpiry(response.getAccessToken())).isNotNull();
    }

    @Test
    void manager_verifyManager_wrongPassword_fails() {
        user("manage2", "MANAGER");
        authenticateAs("manage2", "MANAGER");

        assertThatThrownBy(() -> authService.verifyManager("manage2", password("wrong-pass")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}