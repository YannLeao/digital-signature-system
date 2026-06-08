package com.example.backend.service.document;

import com.example.backend.domain.User;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.exception.PdfValidationException;
import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PdfSigningServiceTests {

    private final PdfValidatorService pdfValidatorService = mock(PdfValidatorService.class);
    private final UserKeyRepository userKeyRepository = mock(UserKeyRepository.class);
    private final UserKeyEncryptionService encryptionService = mock(UserKeyEncryptionService.class);
    private final DocumentSignatureRepository signatureRepository = mock(DocumentSignatureRepository.class);
    private final PdfSigningService service = new PdfSigningService(
            pdfValidatorService,
            userKeyRepository,
            encryptionService,
            signatureRepository
    );

    @Test
    void invalidPdfStopsBeforeKeyAccessAndSignatureRecord() {
        MockMultipartFile file = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[0]);
        when(pdfValidatorService.validateAndRead(file))
                .thenThrow(new PdfValidationException("DOC_001", "Documento PDF invalido."));

        assertThatThrownBy(() -> service.sign(
                file,
                new SignDocumentRequest(1, BigDecimal.TEN, BigDecimal.TEN),
                user(),
                "127.0.0.1",
                Instant.parse("2026-06-08T12:00:00Z")
        ))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Documento PDF invalido.");

        verify(pdfValidatorService).validateAndRead(file);
        verify(userKeyRepository, never()).findByUserId(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(encryptionService);
        verifyNoInteractions(signatureRepository);
    }

    private User user() {
        return User.register(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "user@example.com",
                "password-hash",
                Instant.parse("2026-06-08T12:00:00Z")
        );
    }
}
