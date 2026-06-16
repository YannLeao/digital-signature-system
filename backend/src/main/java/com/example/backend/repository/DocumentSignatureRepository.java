package com.example.backend.repository;

import com.example.backend.domain.DocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, UUID> {

    Optional<DocumentSignature> findBySignatureId(UUID signatureId);

    Optional<DocumentSignature> findBySignedHash(String signedHash);
}
