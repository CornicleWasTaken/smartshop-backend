package com.shop.simpleshop.services;

import com.shop.simpleshop.dto.auth.AuthResponseDTO;
import com.shop.simpleshop.dto.auth.AuthUserDTO;
import com.shop.simpleshop.dto.auth.LoginRequestDTO;
import com.shop.simpleshop.dto.auth.RefreshResponseDTO;
import com.shop.simpleshop.dto.auth.RegisterRequestDTO;
import com.shop.simpleshop.dto.auth.VerifyManagerRequest;
import com.shop.simpleshop.dto.auth.VerifyManagerResponseDTO;
import com.shop.simpleshop.entity.User;
import com.shop.simpleshop.exceptions.DuplicateUserException;
import com.shop.simpleshop.exceptions.InsufficientRoleException;
import com.shop.simpleshop.exceptions.InvalidCredentialsException;
import com.shop.simpleshop.exceptions.InvalidInputException;
import com.shop.simpleshop.repository.UserRepository;
import com.shop.simpleshop.security.AppRole;
import com.shop.simpleshop.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * Handles user registration, login, token refresh, and session logout.
 *
 * Access tokens are returned in the JSON body (stored client-side by the frontend).
 * Refresh tokens are delivered as an httpOnly cookie scoped to /api/auth so the
 * frontend's {@code /api/auth/refresh} call (sent with credentials) can rotate them
 * without exposing the token to JavaScript.
 */
@Service
public class AuthService {

    public static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final Duration REFRESH_COOKIE_MAX_AGE = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request, HttpServletResponse response) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email '" + request.getEmail() + "' is already registered");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("Password and password confirmation do not match");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("STAFF");
        User saved = userRepository.save(user);

        setRefreshCookie(response, jwtService.generateRefreshToken(saved));
        return toAuthResponse(saved, jwtService.generateAccessToken(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        User user = userRepository.findByUsernameOrEmail(request.getIdentifier(), request.getIdentifier())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        setRefreshCookie(response, jwtService.generateRefreshToken(user));
        return toAuthResponse(user, jwtService.generateAccessToken(user));
    }

    /**
     * Validates the refresh token from the httpOnly cookie and issues a fresh access token.
     *
     * @param refreshToken the raw refresh token (may be null/blank when the cookie is absent)
     */
    public RefreshResponseDTO refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException();
        }

        Long userId = jwtService.parseUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);

        // Rotate the refresh cookie on every refresh.
        setRefreshCookie(response, jwtService.generateRefreshToken(user));
        return new RefreshResponseDTO(jwtService.generateAccessToken(user));
    }

    public AuthUserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        return toAuthUserDTO(user);
    }

    /**
     * Grants a short-lived manager override: verifies that the current user is
     * a MANAGER/ADMIN and that the supplied password matches, then issues a fresh
     * access token carrying an {@code override} claim. Cashier sessions are
     * rejected with {@link InsufficientRoleException}.
     *
     * @param username the currently authenticated user
     * @param request  the password to verify
     */
    public VerifyManagerResponseDTO verifyManager(String username, VerifyManagerRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!AppRole.fromDb(user.getRole()).isElevated()) {
            throw new InsufficientRoleException("Manager credentials required");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new VerifyManagerResponseDTO(
                jwtService.generateOverrideAccessToken(user, user.getUsername()));
    }

    public void logout(HttpServletResponse response) {
        clearRefreshCookie(response);
    }

    private AuthResponseDTO toAuthResponse(User user, String accessToken) {
        return new AuthResponseDTO(accessToken, "Bearer", toAuthUserDTO(user));
    }

    private AuthUserDTO toAuthUserDTO(User user) {
        return AuthUserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(AppRole.fromDb(user.getRole()).name())
                .build();
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, token)
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(REFRESH_COOKIE_MAX_AGE)
                .sameSite("Lax")
                .secure(false) // http in local dev; enable Secure in production
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
