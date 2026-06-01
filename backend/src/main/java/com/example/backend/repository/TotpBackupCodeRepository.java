package com.example.backend.repository;

import com.example.backend.domain.TotpBackupCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TotpBackupCodeRepository extends JpaRepository<TotpBackupCode, UUID> {
    List<TotpBackupCode> findAllByUserId(UUID userId);
    void deleteAllByUserId(UUID userId);
}