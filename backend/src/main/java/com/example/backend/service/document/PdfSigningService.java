package com.example.backend.service.document;

import com.example.backend.domain.DocumentSignature;
import com.example.backend.domain.User;
import com.example.backend.domain.UserKey;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.dto.document.SignDocumentResponse;
import com.example.backend.exception.PdfValidationException;
import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class PdfSigningService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);

    private final PdfValidatorService            pdfValidatorService;
    private final UserKeyRepository              userKeyRepository;
    private final UserKeyEncryptionService       encryptionService;
    private final DocumentSignatureRepository    signatureRepository;

    public PdfSigningService(PdfValidatorService pdfValidatorService,
                              UserKeyRepository userKeyRepository,
                              UserKeyEncryptionService encryptionService,
                              DocumentSignatureRepository signatureRepository) {
        this.pdfValidatorService  = pdfValidatorService;
        this.userKeyRepository    = userKeyRepository;
        this.encryptionService    = encryptionService;
        this.signatureRepository  = signatureRepository;
    }

    @Transactional
    public SignDocumentResponse sign(MultipartFile file,
                                     SignDocumentRequest request,
                                     User user,
                                     String originIp,
                                     Instant now) {
        // 1. Validar PDF
        byte[] originalBytes = pdfValidatorService.validateAndRead(file);

        // 2. Hash SHA-256 do documento original
        String originalHash = sha256Hex(originalBytes);

        // 3. Buscar par de chaves do usuário
        UserKey userKey = userKeyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Par de chaves não encontrado para o usuário."));

        // 4. Descriptografar chave privada
        String privateKeyB64  = encryptionService.decrypt(userKey.getEncryptedPrivateKey());
        PrivateKey privateKey = decodePrivateKey(privateKeyB64, userKey.getKeyAlgorithm());

        // 5. Gerar ID único da assinatura
        UUID signatureId = UUID.randomUUID();

        // 6. Embutir selo visual no PDF em memória
        byte[] sealedBytes = embedSeal(
                originalBytes, user, originalHash, signatureId, now,
                request.sealPage(), request.sealX(), request.sealY());

        // 7. Assinar os bytes do PDF selado
        signBytes(sealedBytes, privateKey, userKey.getKeyAlgorithm());

        // 8. Hash SHA-256 do documento assinado
        String signedHash = sha256Hex(sealedBytes);

        // 9. Registrar no banco
        DocumentSignature record = DocumentSignature.create(
                user, originalHash, signedHash, signatureId,
                userKey.getKeyAlgorithm(),
                request.sealPage(),
                request.sealX(),
                request.sealY(),
                originIp, now);
        signatureRepository.save(record);

        return new SignDocumentResponse(
                signatureId,
                originalHash,
                signedHash,
                TIMESTAMP_FMT.format(now));
    }

    // -------------------------------------------------------------------------

    private byte[] embedSeal(byte[] pdfBytes, User user, String docHash,
                               UUID signatureId, Instant now,
                               int sealPage, BigDecimal sealX, BigDecimal sealY) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            int pageIndex = sealPage - 1;
            if (pageIndex < 0 || pageIndex >= doc.getNumberOfPages()) {
                throw new PdfValidationException("VAL_001",
                        "Página do selo inválida: o PDF possui " +
                        doc.getNumberOfPages() + " página(s).");
            }

            PDPage page = doc.getPage(pageIndex);

            try (PDPageContentStream cs = new PDPageContentStream(
                    doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                PDType1Font font      = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fontBold  = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                float x = sealX.floatValue();
                float y = sealY.floatValue();

                // Fundo do selo
                cs.setNonStrokingColor(0.95f, 0.95f, 0.95f);
                cs.addRect(x - 5, y - 55, 310, 70);
                cs.fill();

                // Borda do selo
                cs.setStrokingColor(0.3f, 0.3f, 0.3f);
                cs.setLineWidth(0.5f);
                cs.addRect(x - 5, y - 55, 310, 70);
                cs.stroke();

                // Linha 1 — Assinante
                cs.beginText();
                cs.setFont(fontBold, 7);
                cs.newLineAtOffset(x, y + 8);
                cs.showText("Assinado digitalmente por: " + user.getEmail());
                cs.endText();

                // Linha 2 — Data/hora
                cs.beginText();
                cs.setFont(font, 7);
                cs.newLineAtOffset(x, y - 4);
                cs.showText("Data/Hora UTC: " + TIMESTAMP_FMT.format(now));
                cs.endText();

                // Linha 3 — Hash do documento
                cs.beginText();
                cs.setFont(font, 6);
                cs.newLineAtOffset(x, y - 16);
                cs.showText("Hash SHA-256: " + docHash.substring(0, 32) + "...");
                cs.endText();

                // Linha 4 — ID da assinatura
                cs.beginText();
                cs.setFont(font, 6);
                cs.newLineAtOffset(x, y - 28);
                cs.showText("ID da assinatura: " + signatureId);
                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();

        } catch (PdfValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao embutir selo visual no PDF.", e);
        }
    }

    private byte[] signBytes(byte[] data, PrivateKey privateKey, String algorithm) {
        try {
            String sigAlgorithm = "RSA".equalsIgnoreCase(algorithm)
                    ? "SHA256withRSA"
                    : "SHA256withECDSA";

            Signature sig = Signature.getInstance(sigAlgorithm);
            sig.initSign(privateKey);
            sig.update(data);
            return sig.sign();
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao assinar o documento.", e);
        }
    }

    private PrivateKey decodePrivateKey(String base64Key, String algorithm) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            String jcaAlgorithm = "RSA".equalsIgnoreCase(algorithm) ? "RSA" : "EC";
            return KeyFactory.getInstance(jcaAlgorithm).generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao decodificar chave privada.", e);
        }
    }

    private String sha256Hex(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao calcular hash SHA-256.", e);
        }
    }
}