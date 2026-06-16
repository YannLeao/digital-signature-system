package com.example.backend.dto.document;

public record VerifyDocumentResponse(
        VerifyStatus status,
        String message,
        VerifySignatureData signature
) {

    public static VerifyDocumentResponse valid(VerifySignatureData signature) {
        return new VerifyDocumentResponse(
                VerifyStatus.VALID,
                "Documento integro e autenticado.",
                signature
        );
    }

    public static VerifyDocumentResponse tampered() {
        return new VerifyDocumentResponse(
                VerifyStatus.TAMPERED,
                "Documento adulterado: a integridade foi comprometida.",
                null
        );
    }

    public static VerifyDocumentResponse notFound() {
        return new VerifyDocumentResponse(
                VerifyStatus.NOT_FOUND,
                "Assinatura nao encontrada neste documento.",
                null
        );
    }
}
