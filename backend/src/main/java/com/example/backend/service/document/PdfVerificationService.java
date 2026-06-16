package com.example.backend.service.document;

import com.example.backend.domain.DocumentSignature;
import com.example.backend.domain.UserKey;
import com.example.backend.dto.document.VerifyDocumentResponse;
import com.example.backend.dto.document.VerifySignatureData;
import com.example.backend.repository.DocumentSignatureRepository;
import com.example.backend.repository.UserKeyRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PdfVerificationService {

    static final String SIGNATURE_ID_CONTACT_PREFIX = "signature-id:";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final PdfValidatorService pdfValidatorService;
    private final DocumentSignatureRepository signatureRepository;
    private final UserKeyRepository userKeyRepository;

    public PdfVerificationService(PdfValidatorService pdfValidatorService,
                                  DocumentSignatureRepository signatureRepository,
                                  UserKeyRepository userKeyRepository) {
        this.pdfValidatorService = pdfValidatorService;
        this.signatureRepository = signatureRepository;
        this.userKeyRepository = userKeyRepository;
    }

    public VerifyDocumentResponse verify(MultipartFile file) {
        byte[] pdfBytes = pdfValidatorService.validateAndRead(file);
        String receivedHash = sha256Hex(pdfBytes);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            List<PDSignature> signatures = document.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                return VerifyDocumentResponse.notFound();
            }

            for (PDSignature signature : signatures) {
                Optional<UUID> signatureId = signatureId(signature);
                if (signatureId.isEmpty()) {
                    continue;
                }

                Optional<DocumentSignature> record = signatureRepository.findBySignatureId(signatureId.get());
                if (record.isPresent()) {
                    return evaluate(record.get(), signature, pdfBytes, receivedHash);
                }
            }

            Optional<DocumentSignature> recordByHash = signatureRepository.findBySignedHash(receivedHash);
            if (recordByHash.isPresent()) {
                return evaluate(recordByHash.get(), signatures.getLast(), pdfBytes, receivedHash);
            }

            return VerifyDocumentResponse.notFound();
        } catch (Exception exception) {
            return VerifyDocumentResponse.notFound();
        }
    }

    private VerifyDocumentResponse evaluate(DocumentSignature record,
                                            PDSignature signature,
                                            byte[] pdfBytes,
                                            String receivedHash) {
        if (!receivedHash.equals(record.getSignedHash())) {
            return VerifyDocumentResponse.tampered();
        }

        UserKey userKey = userKeyRepository.findByUserId(record.getUser().getId())
                .orElse(null);
        if (userKey == null) {
            return VerifyDocumentResponse.notFound();
        }

        PublicKey publicKey = decodePublicKey(userKey.getPublicKey(), userKey.getKeyAlgorithm());
        if (!verifyEmbeddedSignature(signature, pdfBytes, publicKey)) {
            return VerifyDocumentResponse.tampered();
        }

        return VerifyDocumentResponse.valid(new VerifySignatureData(
                record.getSignatureId(),
                record.getSignedAt(),
                record.getUser().getEmail()
        ));
    }

    private Optional<UUID> signatureId(PDSignature signature) {
        String contactInfo = signature.getContactInfo();
        if (contactInfo == null || !contactInfo.startsWith(SIGNATURE_ID_CONTACT_PREFIX)) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(contactInfo.substring(SIGNATURE_ID_CONTACT_PREFIX.length())));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private boolean verifyEmbeddedSignature(PDSignature signature, byte[] pdfBytes, PublicKey expectedPublicKey) {
        try {
            byte[] contents = signature.getContents(pdfBytes);
            byte[] signedContent = signature.getSignedContent(pdfBytes);
            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(signedContent), contents);
            SignerInformationStore signerInfos = signedData.getSignerInfos();

            for (SignerInformation signer : signerInfos.getSigners()) {
                @SuppressWarnings("unchecked")
                Collection<X509CertificateHolder> certificates =
                        (Collection<X509CertificateHolder>) signedData.getCertificates().getMatches(signer.getSID());
                if (certificates.isEmpty()) {
                    return false;
                }

                X509CertificateHolder certificate = certificates.iterator().next();
                PublicKey embeddedPublicKey = KeyFactory.getInstance(expectedPublicKey.getAlgorithm())
                        .generatePublic(new X509EncodedKeySpec(certificate.getSubjectPublicKeyInfo().getEncoded()));

                if (!Arrays.equals(embeddedPublicKey.getEncoded(), expectedPublicKey.getEncoded())) {
                    return false;
                }

                return signer.verify(new JcaSimpleSignerInfoVerifierBuilder()
                        .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .build(expectedPublicKey));
            }

            return false;
        } catch (Exception exception) {
            return false;
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
        if ("RSA".equalsIgnoreCase(algorithm)) {
            return "RSA";
        }
        if ("ECDSA".equalsIgnoreCase(algorithm) || "EC".equalsIgnoreCase(algorithm)) {
            return "EC";
        }
        throw new IllegalStateException("Algoritmo de chave nao suportado.");
    }

    private String sha256Hex(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException("Erro ao calcular hash do PDF.", exception);
        }
    }
}
