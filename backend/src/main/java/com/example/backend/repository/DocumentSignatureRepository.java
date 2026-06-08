package com.example.backend.repository;

import com.example.backend.domain.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, UUID> {
}