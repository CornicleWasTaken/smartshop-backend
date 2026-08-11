package com.shop.simpleshop.security;

import com.shop.simpleshop.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Generates and parses HS256 JWTs for access and refresh tokens.
 */
@Service
public class JwtService {

    /** Claim holding the epoch-millis time an override grant expires. */
    public static final String OVERRIDE_CLAIM = "override";
    /** Claim holding the username of the manager who granted an override. */
    public static final String OVERRIDE_BY_CLAIM = "overrideBy";

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;
    private final long overrideMinutes;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
                      @Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs,
                      @Value("${app.auth.override-minutes}") long overrideMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
        this.overrideMinutes = overrideMinutes;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessExpirationMs, null, null);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpirationMs, null, null);
    }

    /**
     * Issues a fresh access token carrying a short-lived {@code override} claim.
     * Used by the manager-override flow; the override is NOT refreshable and
     * expires after {@code app.auth.override-minutes}.
     *
     * @param user the user to elevate
     * @param overrideBy username of the manager granting the override
     */
    public String generateOverrideAccessToken(User user, String overrideBy) {
        Instant overrideExpiry = Instant.now().plusSeconds(overrideMinutes * 60L);
        return buildToken(user, accessExpirationMs, overrideExpiry, overrideBy);
    }

    /**
     * Extracts the subject (user ID) from a token.
     *
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Long parseUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    /**
     * Extracts the role claim from a token.
     *
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String parseRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Returns the override expiry for a token, or {@code null} if the token does
     * not carry an override claim.
     */
    public Instant parseOverrideExpiry(String token) {
        Number expiry = parseClaims(token).get(OVERRIDE_CLAIM, Number.class);
        return expiry == null ? null : Instant.ofEpochMilli(expiry.longValue());
    }

    /**
     * Returns the username of the manager who granted the override, or
     * {@code null} if the token does not carry one.
     */
    public String parseOverrideBy(String token) {
        return parseClaims(token).get(OVERRIDE_BY_CLAIM, String.class);
    }

    private String buildToken(User user, long expirationMs, Instant overrideExpiry, String overrideBy) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("role", user.getRole())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key);
        if (overrideExpiry != null) {
            builder.claim(OVERRIDE_CLAIM, overrideExpiry.toEpochMilli());
        }
        if (overrideBy != null) {
            builder.claim(OVERRIDE_BY_CLAIM, overrideBy);
        }
        return builder.compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}