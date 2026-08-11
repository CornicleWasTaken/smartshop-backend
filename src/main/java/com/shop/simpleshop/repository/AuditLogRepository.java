package com.shop.simpleshop.repository;

import com.shop.simpleshop.security.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
}