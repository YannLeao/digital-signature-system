package com.example.backend.repository;

import com.example.backend.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Override
    @Modifying
    default void deleteById(UUID uuid) {
        throw new UnsupportedOperationException("audit_log e append-only.");
    }

    @Override
    @Modifying
    default void delete(AuditLog entity) {
        throw new UnsupportedOperationException("audit_log e append-only.");
    }

    @Override
    @Modifying
    default void deleteAll() {
        throw new UnsupportedOperationException("audit_log e append-only.");
    }
}