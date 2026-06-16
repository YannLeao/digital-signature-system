package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Service
public class PdfValidatorService {

    private static final String ERROR_CODE = "DOC_001";
    private static final long MAX_SIZE_BYTES = 20L * 1024 * 1024;
    private static final byte[] PDF_MAGIC = new byte[]{'%', 'P', 'D', 'F', '-'};
    private static final Pattern SUSPICIOUS_PDF_PATTERN = Pattern.compile(
            "/JavaScript\\b|/JS\\b|/OpenAction\\b|/AA\\b",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<[^>]*script[^>]*>|javascript:|on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern SQL_PATTERN = Pattern.compile(
            "('\\s*(--|;|\\bOR\\b|\\bAND\\b))|\\bUNION\\b|\\bSELECT\\b|\\bINSERT\\b|\\bDROP\\b|\\bDELETE\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final PdfSandboxService pdfSandboxService;

    public PdfValidatorService() {
        this(new PdfSandboxService(false, java.time.Duration.ofSeconds(5), "", ""));
    }

    @Autowired
    public PdfValidatorService(PdfSandboxService pdfSandboxService) {
        this.pdfSandboxService = pdfSandboxService;
    }

    public byte[] validateAndRead(MultipartFile file) {
        validateFileMetadata(file);

        byte[] bytes = readBytes(file);
        validateMagicNumber(bytes);
        validateNoSuspiciousActions(bytes);
        pdfSandboxService.validateStructure(bytes);

        return bytes;
    }

    public String sanitizeText(String input) {
        if (input == null) {
            return null;
        }

        if (XSS_PATTERN.matcher(input).find()) {
            throw invalid("Input contem conteudo potencialmente malicioso.");
        }
        if (SQL_PATTERN.matcher(input).find()) {
            throw invalid("Input contem conteudo potencialmente malicioso.");
        }

        return input
                .replaceAll("<[^>]*>", "")
                .replaceAll("\\p{Cntrl}", "")
                .replaceAll("[<>\"']", "")
                .trim();
    }

    private void validateFileMetadata(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw invalid("Documento PDF invalido.");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw invalid("Arquivo excede o tamanho maximo permitido de 20MB.");
        }
        if (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())) {
            throw invalid("Documento PDF invalido.");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw invalid("Nao foi possivel ler o arquivo enviado.");
        }
    }

    private void validateMagicNumber(byte[] bytes) {
        if (bytes.length < PDF_MAGIC.length) {
            throw invalid("Arquivo invalido: nao e um PDF.");
        }

        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                throw invalid("Arquivo invalido: magic number %PDF- nao encontrado.");
            }
        }
    }

    private void validateNoSuspiciousActions(byte[] bytes) {
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);
        if (SUSPICIOUS_PDF_PATTERN.matcher(raw).find()) {
            throw invalid("PDF rejeitado: contem acoes embutidas suspeitas.");
        }

        // Parsing estrutural do PDF acontece em processo isolado via PdfSandboxService.
    }

    private PdfValidationException invalid(String message) {
        return new PdfValidationException(ERROR_CODE, message);
    }
}
