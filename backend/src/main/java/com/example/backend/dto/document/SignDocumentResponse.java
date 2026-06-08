package com.example.backend.dto.document;

import java.util.UUID;

public record SignDocumentResponse(
        UUID   signatureId,
        String originalHash,
        String signedHash,
        String signedAt
) {}