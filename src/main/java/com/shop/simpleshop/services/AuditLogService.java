package com.shop.simpleshop.services;

import com.shop.simpleshop.repository.AuditLogRepository;
import com.shop.simpleshop.security.AuditLogEntry;
import com.shop.simpleshop.security.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Records sensitive actions to the append-only audit log. The actor is the
 * authenticated username; when the caller holds the {@code OVERRIDE} authority,
 * the overrider's username (carried on {@code Authentication.getDetails()} by the
 * JWT filter) is recorded alongside so the true authorizer is never hidden.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records an operation performed by the current authenticated user.
     * Runs in its own transaction (REQUIRES_NEW) so the audit row survives even
     * if the surrounding business transaction later rolls back.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordCurrentUser(Operation action, String resourceType, Long resourceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        AuditLogEntry entry = new AuditLogEntry();
        entry.setActor(authentication.getName());
        entry.setAction(action);
        entry.setResourceType(resourceType);
        entry.setResourceId(resourceId);
        entry.setOverrideBy(currentOverrideBy(authentication));
        entry.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(entry);
    }

    /**
     * Returns the overrider's username when the current caller is acting under a
     * manager override, otherwise {@code null}. Useful for recording who actually
     * authorized an override-eligible action (e.g. voids).
     */
    public String currentOverrideBy() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return currentOverrideBy(authentication);
    }

    private String currentOverrideBy(Authentication authentication) {
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> "OVERRIDE".equals(a.getAuthority()))) {
            return null;
        }
        Object details = authentication.getDetails();
        return details instanceof String username ? username : null;
    }
}