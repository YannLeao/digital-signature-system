package com.example.backend.repository;

import com.example.backend.domain.JwtDenylistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JwtDenylistRepository extends JpaRepository<JwtDenylistEntry, UUID> {

	boolean existsByJti(String jti);
}
