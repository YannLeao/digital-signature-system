package com.example.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_signatures")
public class DocumentSignature {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "original_hash", nullable = false, length = 64)
    private String originalHash;

    @Column(name = "signed_hash", nullable = false, length = 64)
    private String signedHash;

    @Column(name = "signature_id", nullable = false, updatable = false)
    private UUID signatureId;

    @Column(name = "key_algorithm", nullable = false, length = 20)
    private String keyAlgorithm;

    @Column(name = "seal_page", nullable = false)
    private int sealPage;

    @Column(name = "seal_x", nullable = false, precision = 10, scale = 2)
    private BigDecimal sealX;

    @Column(name = "seal_y", nullable = false, precision = 10, scale = 2)
    private BigDecimal sealY;

    @Column(name = "origin_ip", nullable = false, length = 45)
    private String originIp;

    @Column(name = "signed_at", nullable = false, updatable = false)
    private Instant signedAt;

    protected DocumentSignature() {}

    private DocumentSignature(UUID id, User user, String originalHash, String signedHash,
                               UUID signatureId, String keyAlgorithm,
                               int sealPage, BigDecimal sealX, BigDecimal sealY,
                               String originIp, Instant signedAt) {
        this.id           = id;
        this.user         = user;
        this.originalHash = originalHash;
        this.signedHash   = signedHash;
        this.signatureId  = signatureId;
        this.keyAlgorithm = keyAlgorithm;
        this.sealPage     = sealPage;
        this.sealX        = sealX;
        this.sealY        = sealY;
        this.originIp     = originIp;
        this.signedAt     = signedAt;
    }

    public static DocumentSignature create(User user, String originalHash, String signedHash,
                                            UUID signatureId, String keyAlgorithm,
                                            int sealPage, BigDecimal sealX, BigDecimal sealY,
                                            String originIp, Instant signedAt) {
        return new DocumentSignature(UUID.randomUUID(), user, originalHash, signedHash,
                signatureId, keyAlgorithm, sealPage, sealX, sealY, originIp, signedAt);
    }

    public UUID getId()            { return id; }
    public User getUser()          { return user; }
    public String getOriginalHash(){ return originalHash; }
    public String getSignedHash()  { return signedHash; }
    public UUID getSignatureId()   { return signatureId; }
    public String getKeyAlgorithm(){ return keyAlgorithm; }
    public int getSealPage()       { return sealPage; }
    public BigDecimal getSealX()   { return sealX; }
    public BigDecimal getSealY()   { return sealY; }
    public String getOriginIp()    { return originIp; }
    public Instant getSignedAt()   { return signedAt; }
}