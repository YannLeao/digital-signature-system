package com.example.backend.controller.document;

import com.example.backend.domain.User;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.dto.document.SignedPdfResult;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.document.PdfSigningService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DocumentControllerTests {

    private final PdfSigningService pdfSigningService = mock(PdfSigningService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final AuditService auditService = mock(AuditService.class);
    private final DocumentController controller = new DocumentController(
            pdfSigningService,
            userRepository,
            objectMapper,
            validator,
            auditService
    );

    @Test
    void validUploadReturnsSignedPdfBytesAndMetadataHeaders() throws Exception {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID signatureId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        User user = User.register(userId, "user@example.com", "password-hash", Instant.parse("2026-06-08T12:00:00Z"));
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", "%PDF-".getBytes());
        SignDocumentRequest request = new SignDocumentRequest(1, BigDecimal.TEN, BigDecimal.TEN);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(userId.toString())
                .build();
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        byte[] signedPdf = "%PDF-signed".getBytes();
        SignedPdfResult result = new SignedPdfResult(
                signedPdf,
                signatureId,
                "a".repeat(64),
                "b".repeat(64),
                Instant.parse("2026-06-08T12:00:00Z")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(servletRequest.getHeader("User-Agent")).thenReturn("JUnit/5");
        when(pdfSigningService.sign(eq(file), eq(request), eq(user), eq("127.0.0.1"), any(Instant.class)))
                .thenReturn(result);

        ResponseEntity<byte[]> response = controller.sign(
                file,
                objectMapper.writeValueAsString(request),
                jwt,
                servletRequest
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .isEqualTo("attachment; filename=\"signed-document.pdf\"");
        assertThat(response.getHeaders().getFirst("X-Signature-Id")).isEqualTo(signatureId.toString());
        assertThat(response.getHeaders().getFirst("X-Original-Hash")).isEqualTo("a".repeat(64));
        assertThat(response.getHeaders().getFirst("X-Signed-Hash")).isEqualTo("b".repeat(64));
        assertThat(response.getBody()).isEqualTo(signedPdf);
    }
}
