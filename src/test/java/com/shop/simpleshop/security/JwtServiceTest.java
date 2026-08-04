package com.shop.simpleshop.security;

import com.shop.simpleshop.entity.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("unit-test-secret-unit-test-secret-1234567890", 900_000L, 604_800_000L);
    }

    private User user(Long id, String role) {
        User user = new User();
        user.setUserId(id);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setRole(role);
        return user;
    }

    @Test
    void accessToken_containsUserClaims() {
        String token = jwtService.generateAccessToken(user(42L, "STAFF"));

        assertThat(jwtService.parseUserId(token)).isEqualTo(42L);
        assertThat(jwtService.parseRole(token)).isEqualTo("STAFF");
    }

    @Test
    void refreshToken_isDistinctFromAccessToken() {
        User u = user(42L, "ADMIN");
        String access = jwtService.generateAccessToken(u);
        String refresh = jwtService.generateRefreshToken(u);

        assertThat(refresh).isNotEqualTo(access);
        assertThat(jwtService.parseUserId(refresh)).isEqualTo(42L);
    }

    @Test
    void invalidToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.parseUserId("not.a.token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedToken_throwsJwtException() {
        String token = jwtService.generateAccessToken(user(7L, "STAFF"));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parseUserId(tampered))
                .isInstanceOf(JwtException.class);
    }
}
