package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.apache.pdfbox.pdmodel.interactive.action.PDDocumentCatalogAdditionalActions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Pattern;

@Service
public class PdfValidatorService {

    private static final long   MAX_SIZE_BYTES  = 20L * 1024 * 1024; // 20MB
    private static final byte[] PDF_MAGIC       = new byte[]{'%', 'P', 'D', 'F', '-'};
    private static final Pattern JS_PATTERN     = Pattern.compile(
            "/JavaScript|/JS\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_PATTERN    = Pattern.compile(
            "<[^>]*script[^>]*>|javascript:|on\\w+\\s*=",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_PATTERN    = Pattern.compile(
            "('\\s*(--|;|\\bOR\\b|\\bAND\\b))|\\bUNION\\b|\\bSELECT\\b|\\bINSERT\\b|\\bDROP\\b|\\bDELETE\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * Valida e retorna os bytes do PDF.
     * Lança PdfValidationException com código VAL_001 em caso de falha.
     */
    public byte[] validateAndRead(MultipartFile file) {
        validateSize(file);

        byte[] bytes = readBytes(file);

        validateMagicNumber(bytes);
        validateNoJavaScript(bytes);

        return bytes;
    }

    /**
     * Sanitiza um input de texto contra XSS e SQL injection.
     */
    public String sanitizeText(String input) {
        if (input == null) return null;

        if (XSS_PATTERN.matcher(input).find()) {
            throw new PdfValidationException("VAL_001",
                    "Input contém conteúdo potencialmente malicioso (XSS).");
        }
        if (SQL_PATTERN.matcher(input).find()) {
            throw new PdfValidationException("VAL_001",
                    "Input contém conteúdo potencialmente malicioso (SQL injection).");
        }

        // Remove tags HTML e caracteres perigosos
        return input
                .replaceAll("<[^>]*>", "")
                .replaceAll("[<>\"']", "")
                .trim();
    }

    // -------------------------------------------------------------------------

    private void validateSize(MultipartFile file) {
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new PdfValidationException("VAL_001",
                    "Arquivo excede o tamanho máximo permitido de 20MB.");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new PdfValidationException("VAL_001",
                    "Não foi possível ler o arquivo enviado.");
        }
    }

    private void validateMagicNumber(byte[] bytes) {
        if (bytes.length < PDF_MAGIC.length) {
            throw new PdfValidationException("VAL_001",
                    "Arquivo inválido: não é um PDF.");
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                throw new PdfValidationException("VAL_001",
                        "Arquivo inválido: magic number %PDF- não encontrado.");
            }
        }
    }

    private void validateNoJavaScript(byte[] bytes) {
        // Verificação rápida via raw bytes antes de fazer parse completo
        String raw = new String(bytes);
        if (JS_PATTERN.matcher(raw).find()) {
            throw new PdfValidationException("VAL_001",
                    "PDF rejeitado: contém JavaScript embutido.");
        }

        // Verificação estrutural via PDFBox
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDDocumentCatalogAdditionalActions actions =
                    doc.getDocumentCatalog().getActions();

            if (actions != null) {
                checkAction(actions.getDP());
                checkAction(actions.getDS());
                checkAction(actions.getWC());
                checkAction(actions.getWP());
                checkAction(actions.getWS());
            }

            for (PDPage page : doc.getPages()) {
                if (page.getActions() != null) {
                    checkAction(page.getActions().getO());
                    checkAction(page.getActions().getC());
                }
            }
        } catch (PdfValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new PdfValidationException("VAL_001",
                    "Não foi possível processar o PDF para validação.");
        }
    }

    private void checkAction(PDAction action) {
        if (action instanceof PDActionJavaScript) {
            throw new PdfValidationException("VAL_001",
                    "PDF rejeitado: contém JavaScript embutido.");
        }
    }
}