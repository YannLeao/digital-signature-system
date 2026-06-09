package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfValidatorServiceTests {

    private final PdfValidatorService service = new PdfValidatorService();

    @Test
    void acceptsValidPdf() throws Exception {
        byte[] pdf = validPdf();

        byte[] validated = service.validateAndRead(pdfFile(pdf));

        assertThat(validated).isEqualTo(pdf);
    }

    @Test
    void rejectsNullFile() {
        assertThatThrownBy(() -> service.validateAndRead(null))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Documento PDF invalido.");
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = pdfFile(new byte[0]);

        assertThatThrownBy(() -> service.validateAndRead(file))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Documento PDF invalido.");
    }

    @Test
    void rejectsFileAboveTwentyMegabytes() {
        byte[] oversized = new byte[(20 * 1024 * 1024) + 1];
        Arrays.fill(oversized, (byte) ' ');
        System.arraycopy("%PDF-".getBytes(StandardCharsets.US_ASCII), 0, oversized, 0, 5);

        assertThatThrownBy(() -> service.validateAndRead(pdfFile(oversized)))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Arquivo excede o tamanho maximo permitido de 20MB.");
    }

    @Test
    void rejectsWrongContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "text/plain",
                validPdf()
        );

        assertThatThrownBy(() -> service.validateAndRead(file))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Documento PDF invalido.");
    }

    @Test
    void rejectsPdfExtensionWithInvalidMagicNumber() {
        MockMultipartFile file = pdfFile("not a pdf".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> service.validateAndRead(file))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Arquivo invalido: magic number %PDF- nao encontrado.");
    }

    @Test
    void rejectsMalformedPdf() {
        MockMultipartFile file = pdfFile("%PDF-1.7\nmalformed".getBytes(StandardCharsets.US_ASCII));

        assertThatThrownBy(() -> service.validateAndRead(file))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Nao foi possivel processar o PDF para validacao.");
    }

    @Test
    void rejectsPdfWithRawJavascriptMarker() throws Exception {
        byte[] pdf = withAppendedContent(validPdf(), "\n/JavaScript ");

        assertThatThrownBy(() -> service.validateAndRead(pdfFile(pdf)))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("PDF rejeitado: contem acoes embutidas suspeitas.");
    }

    @Test
    void rejectsPdfWithRawJsMarker() throws Exception {
        byte[] pdf = withAppendedContent(validPdf(), "\n/JS ");

        assertThatThrownBy(() -> service.validateAndRead(pdfFile(pdf)))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("PDF rejeitado: contem acoes embutidas suspeitas.");
    }

    @Test
    void rejectsPdfWithRawOpenActionMarker() throws Exception {
        byte[] pdf = withAppendedContent(validPdf(), "\n/OpenAction ");

        assertThatThrownBy(() -> service.validateAndRead(pdfFile(pdf)))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("PDF rejeitado: contem acoes embutidas suspeitas.");
    }

    @Test
    void rejectsPdfWithRawAdditionalActionsMarker() throws Exception {
        byte[] pdf = withAppendedContent(validPdf(), "\n/AA ");

        assertThatThrownBy(() -> service.validateAndRead(pdfFile(pdf)))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("PDF rejeitado: contem acoes embutidas suspeitas.");
    }

    @Test
    void rejectsPdfWithStructuredJavascriptOpenAction() throws Exception {
        byte[] pdf = pdfWithOpenJavascriptAction();

        assertThatThrownBy(() -> service.validateAndRead(pdfFile(pdf)))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("PDF rejeitado: contem acoes embutidas suspeitas.");
    }

    private MockMultipartFile pdfFile(byte[] bytes) {
        return new MockMultipartFile("file", "document.pdf", "application/pdf", bytes);
    }

    private byte[] validPdf() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] pdfWithOpenJavascriptAction() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.getDocumentCatalog().setOpenAction(new PDActionJavaScript("app.alert('x');"));
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] withAppendedContent(byte[] pdf, String content) {
        byte[] marker = content.getBytes(StandardCharsets.ISO_8859_1);
        byte[] result = Arrays.copyOf(pdf, pdf.length + marker.length);
        System.arraycopy(marker, 0, result, pdf.length, marker.length);
        return result;
    }
}
