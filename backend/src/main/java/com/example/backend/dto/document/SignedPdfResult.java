package com.example.backend.dto.document;

import java.time.Instant;
import java.util.UUID;

public record SignedPdfResult(
        byte[] pdfBytes,
        UUID signatureId,
        String originalHash,
        String signedHash,
        Instant signedAt
) {
}
