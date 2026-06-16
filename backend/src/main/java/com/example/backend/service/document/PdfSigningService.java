package com.example.backend.service.document;

import com.example.backend.domain.DocumentSignature;
import com.example.backend.domain.User;
import com.example.backend.domain.UserKey;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.dto.document.SignedPdfResult;
import com.example.backend.exception.PdfValidationException;
import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.UserKeyRepository;
import com.example.backend.security.UserKeyEncryptionService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class PdfSigningService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC);
    private static final float SEAL_WIDTH = 245;
    private static final float SEAL_HEIGHT = 56;
    private static final float SEAL_LEFT_OFFSET = 5;
    private static final float SEAL_TOP_OFFSET = 12;
    private static final float SEAL_BOTTOM_OFFSET = SEAL_HEIGHT - SEAL_TOP_OFFSET;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final PdfValidatorService pdfValidatorService;
    private final UserKeyRepository userKeyRepository;
    private final UserKeyEncryptionService encryptionService;
    private final DocumentSignatureRepository signatureRepository;

    public PdfSigningService(PdfValidatorService pdfValidatorService,
                             UserKeyRepository userKeyRepository,
                             UserKeyEncryptionService encryptionService,
                             DocumentSignatureRepository signatureRepository) {
        this.pdfValidatorService = pdfValidatorService;
        this.userKeyRepository = userKeyRepository;
        this.encryptionService = encryptionService;
        this.signatureRepository = signatureRepository;
    }

    @Transactional
    public SignedPdfResult sign(MultipartFile file,
                                SignDocumentRequest request,
                                User user,
                                String originIp,
                                Instant now) {
        byte[] originalBytes = pdfValidatorService.validateAndRead(file);
        String originalHash = sha256Hex(originalBytes);

        UserKey userKey = userKeyRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("Par de chaves nao encontrado para o usuario."));

        String privateKeyB64 = encryptionService.decrypt(userKey.getEncryptedPrivateKey());
        PrivateKey privateKey = decodePrivateKey(privateKeyB64, userKey.getKeyAlgorithm());
        PublicKey publicKey = decodePublicKey(userKey.getPublicKey(), userKey.getKeyAlgorithm());

        UUID signatureId = UUID.randomUUID();
        byte[] sealedBytes = embedSeal(
                originalBytes, user, originalHash, signatureId, now,
                request.sealPage(), request.sealX(), request.sealY());
        byte[] signedPdfBytes = embedPdfSignature(sealedBytes, user, publicKey, privateKey, userKey.getKeyAlgorithm(), signatureId, now);
        String signedHash = sha256Hex(signedPdfBytes);

        DocumentSignature record = DocumentSignature.create(
                user,
                originalHash,
                signedHash,
                signatureId,
                userKey.getKeyAlgorithm(),
                request.sealPage(),
                request.sealX(),
                request.sealY(),
                originIp,
                now
        );
        signatureRepository.save(record);

        return new SignedPdfResult(signedPdfBytes, signatureId, originalHash, signedHash, now);
    }

    private byte[] embedSeal(byte[] pdfBytes, User user, String docHash,
                             UUID signatureId, Instant now,
                             int sealPage, BigDecimal sealX, BigDecimal sealY) {
        try (PDDocument document = Loader.loadPDF(pdfBytes);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            int pageIndex = sealPage - 1;
            if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) {
                throw new PdfValidationException("DOC_001",
                        "Pagina do selo invalida: o PDF possui " + document.getNumberOfPages() + " pagina(s).");
            }

            PDPage page = document.getPage(pageIndex);
            float x = sealX.floatValue();
            float y = sealY.floatValue();
            validateSealPosition(page, x, y);

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                String signer = pdfValidatorService.sanitizeText(user.getEmail());

                float sealLeft = x - SEAL_LEFT_OFFSET;
                float sealBottom = y - SEAL_BOTTOM_OFFSET;

                contentStream.setNonStrokingColor(0.04f, 0.07f, 0.13f);
                contentStream.addRect(sealLeft, sealBottom, SEAL_WIDTH, SEAL_HEIGHT);
                contentStream.fill();

                contentStream.setStrokingColor(0.02f, 0.71f, 0.83f);
                contentStream.setLineWidth(1f);
                contentStream.addRect(sealLeft, sealBottom, SEAL_WIDTH, SEAL_HEIGHT);
                contentStream.stroke();

                drawSealLogo(contentStream, sealLeft + 8, sealBottom + 14);

                float textX = sealLeft + 43;
                contentStream.setNonStrokingColor(0.98f, 0.98f, 0.98f);
                writeText(contentStream, fontBold, 7, textX, y + 2, "Assinado digitalmente");
                writeText(contentStream, font, 6, textX, y - 9, signer);
                contentStream.setNonStrokingColor(0.82f, 0.87f, 0.91f);
                writeText(contentStream, font, 5.5f, textX, y - 20, "UTC " + TIMESTAMP_FMT.format(now));
                writeText(contentStream, font, 5.2f, textX, y - 31, "SHA-256 " + docHash.substring(0, 24) + "...");
            }

            document.save(output);
            return output.toByteArray();
        } catch (PdfValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao embutir selo visual no PDF.", exception);
        }
    }

    private void validateSealPosition(PDPage page, float x, float y) {
        PDRectangle mediaBox = page.getMediaBox();
        if (x < SEAL_LEFT_OFFSET
                || y < SEAL_BOTTOM_OFFSET
                || x - SEAL_LEFT_OFFSET + SEAL_WIDTH > mediaBox.getWidth()
                || y + SEAL_TOP_OFFSET > mediaBox.getHeight()) {
            throw new PdfValidationException("DOC_001", "Posicao do selo invalida.");
        }
    }

    private void drawSealLogo(PDPageContentStream contentStream, float x, float y)
            throws java.io.IOException {
        contentStream.setNonStrokingColor(0.02f, 0.71f, 0.83f);
        contentStream.addRect(x, y, 26, 26);
        contentStream.fill();

        contentStream.setStrokingColor(0.04f, 0.07f, 0.13f);
        contentStream.setLineWidth(2.6f);
        contentStream.moveTo(x + 7, y + 13);
        contentStream.lineTo(x + 11.5f, y + 8.5f);
        contentStream.lineTo(x + 20, y + 18);
        contentStream.stroke();
    }

    private void writeText(PDPageContentStream contentStream, PDType1Font font, float size, float x, float y, String text)
            throws java.io.IOException {
        contentStream.beginText();
        contentStream.setFont(font, size);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private byte[] embedPdfSignature(byte[] pdfBytes,
                                     User user,
                                     PublicKey publicKey,
                                     PrivateKey privateKey,
                                     String keyAlgorithm,
                                     UUID signatureId,
                                     Instant now) {
        try (PDDocument document = Loader.loadPDF(pdfBytes);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            X509Certificate certificate = selfSignedCertificate(user, publicKey, privateKey, keyAlgorithm, now);

            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(pdfValidatorService.sanitizeText(user.getEmail()));
            signature.setReason("Assinatura digital do documento");
            signature.setContactInfo(PdfVerificationService.SIGNATURE_ID_CONTACT_PREFIX + signatureId);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(now));
            signature.setSignDate(calendar);

            document.addSignature(signature, content -> buildCmsSignature(content, certificate, privateKey, keyAlgorithm));
            document.saveIncremental(output);
            return output.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao assinar o documento.", exception);
        }
    }

    private byte[] buildCmsSignature(InputStream content,
                                     X509Certificate certificate,
                                     PrivateKey privateKey,
                                     String keyAlgorithm) {
        try {
            String signatureAlgorithm = "RSA".equalsIgnoreCase(keyAlgorithm) ? "SHA256withRSA" : "SHA256withECDSA";
            ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(privateKey);

            CMSTypedData cmsData = new CMSProcessableByteArray(content.readAllBytes());
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .build()
            ).build(signer, certificate));
            generator.addCertificates(new JcaCertStore(List.of(certificate)));

            CMSSignedData signedData = generator.generate(cmsData, false);
            return signedData.getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao gerar assinatura CMS do PDF.", exception);
        }
    }

    private X509Certificate selfSignedCertificate(User user,
                                                  PublicKey publicKey,
                                                  PrivateKey privateKey,
                                                  String keyAlgorithm,
                                                  Instant now) {
        try {
            String signatureAlgorithm = "RSA".equalsIgnoreCase(keyAlgorithm) ? "SHA256withRSA" : "SHA256withECDSA";
            X500Name subject = new X500Name("CN=" + sanitizeCertificateName(user.getEmail()));
            BigInteger serial = new BigInteger(128, SECURE_RANDOM).abs().add(BigInteger.ONE);
            Date notBefore = Date.from(now.minusSeconds(60));
            Date notAfter = Date.from(now.plusSeconds(10L * 365 * 24 * 60 * 60));

            ContentSigner signer = new JcaContentSignerBuilder(signatureAlgorithm)
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .build(privateKey);
            X509CertificateHolder holder = new JcaX509v3CertificateBuilder(
                    subject,
                    serial,
                    notBefore,
                    notAfter,
                    subject,
                    publicKey
            ).build(signer);

            return new JcaX509CertificateConverter()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate(holder);
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao criar certificado de assinatura do PDF.", exception);
        }
    }

    private String sanitizeCertificateName(String value) {
        String sanitized = pdfValidatorService.sanitizeText(value);
        return sanitized == null || sanitized.isBlank() ? "Usuario" : sanitized.replaceAll("[,=+<>#;\\\\\"]", "");
    }

    private PrivateKey decodePrivateKey(String base64Key, String algorithm) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(jcaAlgorithm(algorithm)).generatePrivate(spec);
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao decodificar chave privada.", exception);
        }
    }

    private PublicKey decodePublicKey(String base64Key, String algorithm) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance(jcaAlgorithm(algorithm)).generatePublic(spec);
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao decodificar chave publica.", exception);
        }
    }

    private String jcaAlgorithm(String algorithm) {
        return "RSA".equalsIgnoreCase(algorithm) ? "RSA" : "EC";
    }

    private String sha256Hex(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao calcular hash SHA-256.", exception);
        }
    }
}
