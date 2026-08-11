package com.shop.simpleshop.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Append-only record of a sensitive action (delete, void, drawer op, role change).
 * The actor and optional overrider are captured from the security context at the
 * time the action happened — never mutated afterwards.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "AUDIT_LOG_ENTRY")
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Column(name = "ACTOR", nullable = false, length = 200)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACTION", nullable = false, length = 40)
    private Operation action;

    @Column(name = "RESOURCE_TYPE", nullable = false, length = 40)
    private String resourceType;

    @Column(name = "RESOURCE_ID")
    private Long resourceId;

    @Column(name = "OVERRIDE_BY", length = 200)
    private String overrideBy;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}