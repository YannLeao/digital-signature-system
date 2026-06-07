package com.example.backend.service.auth;

import com.example.backend.domain.TotpBackupCode;
import com.example.backend.domain.User;
import com.example.backend.dto.auth.TotpSetupResponse;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.TotpEncryptionService;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class TotpSetupService {

    private static final int SECRET_LENGTH = 32;
    private static final int BACKUP_CODE_COUNT = 10;
    private static final int BACKUP_CODE_BYTES = 10;

    private final UserRepository userRepository;
    private final TotpBackupCodeRepository backupCodeRepository;
    private final TotpEncryptionService totpEncryptionService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final SecureRandom secureRandom;
    private final String issuer;

    @Autowired
    public TotpSetupService(
            UserRepository userRepository,
            TotpBackupCodeRepository backupCodeRepository,
            TotpEncryptionService totpEncryptionService,
            PasswordEncoder passwordEncoder,
            @Value("${JWT_ISSUER}") String issuer
    ) {
        this(userRepository, backupCodeRepository, totpEncryptionService,
                passwordEncoder, Clock.systemUTC(), new SecureRandom(), issuer);
    }

    TotpSetupService(
            UserRepository userRepository,
            TotpBackupCodeRepository backupCodeRepository,
            TotpEncryptionService totpEncryptionService,
            PasswordEncoder passwordEncoder,
            Clock clock,
            SecureRandom secureRandom,
            String issuer
    ) {
        this.userRepository = userRepository;
        this.backupCodeRepository = backupCodeRepository;
        this.totpEncryptionService = totpEncryptionService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.secureRandom = secureRandom;
        this.issuer = issuer;
    }

    @Transactional
    public TotpSetupResponse setup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String secret = new DefaultSecretGenerator(SECRET_LENGTH).generate();
        String encryptedSecret = totpEncryptionService.encrypt(secret);

        Instant now = Instant.now(clock);
        user.enableTotp(encryptedSecret, now);
        userRepository.save(user);

        QrData qrData = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        backupCodeRepository.deleteAllByUserId(userId);
        List<String> rawCodes = new ArrayList<>(BACKUP_CODE_COUNT);
        List<TotpBackupCode> entities = new ArrayList<>(BACKUP_CODE_COUNT);

        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            String raw = generateBackupCode();
            entities.add(TotpBackupCode.create(UUID.randomUUID(), user, passwordEncoder.encode(raw)));
            rawCodes.add(raw);
        }
        backupCodeRepository.saveAll(entities);

        return new TotpSetupResponse(qrData.getUri(), rawCodes);
    }

    private String generateBackupCode() {
        byte[] bytes = new byte[BACKUP_CODE_BYTES];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes).toUpperCase();
    }
}
