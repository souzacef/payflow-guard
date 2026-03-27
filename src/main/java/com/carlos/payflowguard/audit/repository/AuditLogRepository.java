package com.carlos.payflowguard.audit.repository;

import com.carlos.payflowguard.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}