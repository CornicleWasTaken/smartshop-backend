package com.shop.simpleshop.security;

import com.shop.simpleshop.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Reads the {@code Authorization: Bearer <token>} header, validates the JWT,
 * and populates the {@link SecurityContextHolder} with the user's authentication.
 * Invalid or expired tokens are left unauthenticated (the security entry point
 * then returns 401 for protected endpoints).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Long userId = jwtService.parseUserId(token);
            userRepository.findById(userId).ifPresent(user -> {
                var authorities = new ArrayList<SimpleGrantedAuthority>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + AppRole.fromDb(user.getRole()).name()));
                // The OVERRIDE authority travels in the token (not the DB user), so it is
                // validated against the token's override claim here rather than the role.
                java.time.Instant overrideExpiry = jwtService.parseOverrideExpiry(token);
                if (overrideExpiry != null && overrideExpiry.isAfter(java.time.Instant.now())) {
                    authorities.add(new SimpleGrantedAuthority("OVERRIDE"));
                }
                var authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
                // Carry the overrider's username (if any) so service code can record it for audit.
                if (jwtService.parseOverrideBy(token) != null) {
                    authentication.setDetails(jwtService.parseOverrideBy(token));
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
