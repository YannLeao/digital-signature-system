package com.example.backend.repository;

import com.example.backend.domain.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, UUID> {

    List<ActiveSession> findByUserIdAndIsActiveTrue(UUID userId);
}