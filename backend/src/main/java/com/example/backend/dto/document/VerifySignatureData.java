package com.example.backend.dto.document;

import java.time.Instant;
import java.util.UUID;

public record VerifySignatureData(
        UUID signatureId,
        Instant signedAt,
        String signerName
) {
}
