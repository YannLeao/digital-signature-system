package com.example.backend.service.document;

import com.example.backend.domain.DocumentSignature;
import com.example.backend.domain.User;
import com.example.backend.domain.UserKey;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.dto.document.SignedPdfResult;
import com.example.backend.dto.document.VerifyStatus;
import com.example.backend.exception.PdfValidationException;
import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PdfVerificationServiceTests {

    private static final Instant NOW = Instant.parse("2026-06-08T12:00:00Z");

    private final PdfValidatorService validator = mock(PdfValidatorService.class);
    private final DocumentSignatureRepository signatureRepository = mock(DocumentSignatureRepository.class);
    private final UserKeyRepository userKeyRepository = mock(UserKeyRepository.class);
    private final PdfVerificationService service = new PdfVerificationService(
            validator,
            signatureRepository,
            userKeyRepository
    );

    @Test
    void signedPdfWithKnownRecordAndValidEmbeddedSignatureReturnsValid() throws Exception {
        Fixture fixture = signedFixture();
        MockMultipartFile file = pdfFile(fixture.signedPdf());
        when(validator.validateAndRead(file)).thenReturn(fixture.signedPdf());
        when(signatureRepository.findBySignatureId(fixture.record().getSignatureId()))
                .thenReturn(Optional.of(fixture.record()));
        when(userKeyRepository.findByUserId(fixture.user().getId()))
                .thenReturn(Optional.of(fixture.userKey()));

        var response = service.verify(file);

        assertThat(response.status()).isEqualTo(VerifyStatus.VALID);
        assertThat(response.signature()).isNotNull();
        assertThat(response.signature().signatureId()).isEqualTo(fixture.record().getSignatureId());
        assertThat(response.signature().signedAt()).isEqualTo(NOW);
        assertThat(response.signature().signerName()).isEqualTo(fixture.user().getEmail());
    }

    @Test
    void unsignedPdfReturnsNotFound() throws Exception {
        byte[] unsignedPdf = validPdf();
        MockMultipartFile file = pdfFile(unsignedPdf);
        when(validator.validateAndRead(file)).thenReturn(unsignedPdf);

        var response = service.verify(file);

        assertThat(response.status()).isEqualTo(VerifyStatus.NOT_FOUND);
        verify(signatureRepository, never()).findBySignatureId(any());
    }

    @Test
    void signedPdfWithUnknownSignatureIdReturnsNotFound() throws Exception {
        Fixture fixture = signedFixture();
        MockMultipartFile file = pdfFile(fixture.signedPdf());
        when(validator.validateAndRead(file)).thenReturn(fixture.signedPdf());
        when(signatureRepository.findBySignatureId(fixture.record().getSignatureId()))
                .thenReturn(Optional.empty());
        when(signatureRepository.findBySignedHash(fixture.result().signedHash()))
                .thenReturn(Optional.empty());

        var response = service.verify(file);

        assertThat(response.status()).isEqualTo(VerifyStatus.NOT_FOUND);
    }

    @Test
    void tamperedPdfWithKnownSignatureIdReturnsTampered() throws Exception {
        Fixture fixture = signedFixture();
        byte[] tamperedPdf = Arrays.copyOf(fixture.signedPdf(), fixture.signedPdf().length + 12);
        System.arraycopy("% tampered".getBytes(), 0, tamperedPdf, fixture.signedPdf().length, "% tampered".length());
        MockMultipartFile file = pdfFile(tamperedPdf);
        when(validator.validateAndRead(file)).thenReturn(tamperedPdf);
        when(signatureRepository.findBySignatureId(fixture.record().getSignatureId()))
                .thenReturn(Optional.of(fixture.record()));

        var response = service.verify(file);

        assertThat(response.status()).isEqualTo(VerifyStatus.TAMPERED);
        verify(userKeyRepository, never()).findByUserId(any());
    }

    @Test
    void divergentRegisteredHashReturnsTampered() throws Exception {
        Fixture fixture = signedFixtureWithRegisteredHash("0".repeat(64));
        MockMultipartFile file = pdfFile(fixture.signedPdf());
        when(validator.validateAndRead(file)).thenReturn(fixture.signedPdf());
        when(signatureRepository.findBySignatureId(fixture.record().getSignatureId()))
                .thenReturn(Optional.of(fixture.record()));

        var response = service.verify(file);

        assertThat(response.status()).isEqualTo(VerifyStatus.TAMPERED);
        verify(userKeyRepository, never()).findByUserId(any());
    }

    @Test
    void invalidPdfIsRejectedByValidator() {
        MockMultipartFile file = pdfFile(new byte[0]);
        when(validator.validateAndRead(file))
                .thenThrow(new PdfValidationException("DOC_001", "Documento PDF invalido."));

        assertThatThrownBy(() -> service.verify(file))
                .isInstanceOf(PdfValidationException.class)
                .hasMessage("Documento PDF invalido.");
    }

    private Fixture signedFixture() throws Exception {
        return signedFixtureWithRegisteredHash(null);
    }

    private Fixture signedFixtureWithRegisteredHash(String registeredSignedHash) throws Exception {
        byte[] originalPdf = validPdf();
        KeyPair keyPair = rsaKeyPair();
        User user = user();
        UserKey userKey = UserKey.create(
                user,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                "encrypted-private-key",
                "RSA",
                NOW
        );
        PdfValidatorService signingValidator = mock(PdfValidatorService.class);
        UserKeyRepository signingUserKeyRepository = mock(UserKeyRepository.class);
        UserKeyEncryptionService encryptionService = mock(UserKeyEncryptionService.class);
        DocumentSignatureRepository signingSignatureRepository = mock(DocumentSignatureRepository.class);
        PdfSigningService signingService = new PdfSigningService(
                signingValidator,
                signingUserKeyRepository,
                encryptionService,
                signingSignatureRepository
        );
        MockMultipartFile file = pdfFile(originalPdf);
        when(signingValidator.validateAndRead(file)).thenReturn(originalPdf);
        when(signingValidator.sanitizeText(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(signingUserKeyRepository.findByUserId(user.getId())).thenReturn(Optional.of(userKey));
        when(encryptionService.decrypt("encrypted-private-key"))
                .thenReturn(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));

        SignedPdfResult result = signingService.sign(
                file,
                new SignDocumentRequest(1, BigDecimal.valueOf(80), BigDecimal.valueOf(150)),
                user,
                "127.0.0.1",
                NOW
        );

        ArgumentCaptor<DocumentSignature> captor = ArgumentCaptor.forClass(DocumentSignature.class);
        verify(signingSignatureRepository).save(captor.capture());
        DocumentSignature record = captor.getValue();
        if (registeredSignedHash != null) {
            record = DocumentSignature.create(
                    user,
                    record.getOriginalHash(),
                    registeredSignedHash,
                    record.getSignatureId(),
                    record.getKeyAlgorithm(),
                    record.getSealPage(),
                    record.getSealX(),
                    record.getSealY(),
                    record.getOriginIp(),
                    record.getSignedAt()
            );
        }

        return new Fixture(user, userKey, result, record);
    }

    private MockMultipartFile pdfFile(byte[] bytes) {
        return new MockMultipartFile("file", "document.pdf", "application/pdf", bytes);
    }

    private byte[] validPdf() throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }

    private KeyPair rsaKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private User user() {
        return User.register(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "user@example.com",
                "password-hash",
                NOW
        );
    }

    private record Fixture(
            User user,
            UserKey userKey,
            SignedPdfResult result,
            DocumentSignature record
    ) {

        private byte[] signedPdf() {
            return result.pdfBytes();
        }
    }
}
