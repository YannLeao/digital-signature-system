package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PdfSandboxWorker {

    private static final int EXIT_VALIDATION_ERROR = 2;
    private static final int EXIT_PROCESSING_ERROR = 3;

    private PdfSandboxWorker() {
    }

    public static void main(String[] args) {
        try {
            byte[] bytes = System.in.readAllBytes();
            PdfValidationCore.validateStructure(bytes);
        } catch (PdfValidationException exception) {
            System.err.println("PDF_VALIDATION_ERROR:" + encode(exception.getMessage()));
            System.exit(EXIT_VALIDATION_ERROR);
        } catch (Exception exception) {
            System.err.println("PDF_PROCESSING_ERROR:" + encode("Nao foi possivel processar o PDF no sandbox."));
            System.exit(EXIT_PROCESSING_ERROR);
        }
    }

    private static String encode(String message) {
        return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
    }
}
