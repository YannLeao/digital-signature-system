package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfSandboxServiceTests {

    private final PdfSandboxService service = new PdfSandboxService(
            true,
            Duration.ofSeconds(10),
            javaExecutable(),
            System.getProperty("java.class.path")
    );

    @Test
    void acceptsValidPdfInsideIsolatedProcess() throws Exception {
        service.validateStructure(validPdf());
    }

    @Test
    void rejectsMalformedPdfInsideIsolatedProcess() {
        byte[] malformed = "%PDF-1.7\nmalformed".getBytes(StandardCharsets.US_ASCII);

        assertThatThrownBy(() -> service.validateStructure(malformed))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Nao foi possivel processar o PDF para validacao.");
    }

    private byte[] validPdf() throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }

    private String javaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe"
                : "java";
        return java.nio.file.Path.of(System.getProperty("java.home"), "bin", executable).toString();
    }
}
