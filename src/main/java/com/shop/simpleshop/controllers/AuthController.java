package com.shop.simpleshop.controllers;

import com.shop.simpleshop.dto.auth.AuthResponseDTO;
import com.shop.simpleshop.dto.auth.AuthUserDTO;
import com.shop.simpleshop.dto.auth.LoginRequestDTO;
import com.shop.simpleshop.dto.auth.RefreshResponseDTO;
import com.shop.simpleshop.dto.auth.RegisterRequestDTO;
import com.shop.simpleshop.dto.auth.VerifyManagerRequest;
import com.shop.simpleshop.dto.auth.VerifyManagerResponseDTO;
import com.shop.simpleshop.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Endpoints for user authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and obtain an access token")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
                                                 HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request,
                                                    HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user")
    public ResponseEntity<AuthUserDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    /**
     * Grants a short-lived manager override. Callable by any authenticated user,
     * but only succeeds for MANAGER/ADMIN sessions (see {@code AuthService.verifyManager}).
     */
    @PostMapping("/verify-manager")
    @Operation(summary = "Verify manager credentials and issue a short-lived override token")
    public ResponseEntity<VerifyManagerResponseDTO> verifyManager(
            @Valid @RequestBody VerifyManagerRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(authService.verifyManager(authentication.getName(), request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Exchange the refresh-token cookie for a new access token")
    public ResponseEntity<RefreshResponseDTO> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(readRefreshCookie(request), response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Clear the refresh-token cookie")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ResponseEntity.noContent().build();
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (AuthService.REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
