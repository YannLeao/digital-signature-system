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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TotpSetupService {

    private static final int SECRET_BYTES = 20;
    private static final int BACKUP_CODE_COUNT = 10;

    private final UserRepository userRepository;
    private final TotpBackupCodeRepository backupCodeRepository;
    private final TotpEncryptionService totpEncryptionService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String issuer;

    public TotpSetupService(
            UserRepository userRepository,
            TotpBackupCodeRepository backupCodeRepository,
            TotpEncryptionService totpEncryptionService,
            PasswordEncoder passwordEncoder,
            @Value("${JWT_ISSUER}") String issuer
    ) {
        this(userRepository, backupCodeRepository, totpEncryptionService,
                passwordEncoder, Clock.systemUTC(), issuer);
    }

    TotpSetupService(
            UserRepository userRepository,
            TotpBackupCodeRepository backupCodeRepository,
            TotpEncryptionService totpEncryptionService,
            PasswordEncoder passwordEncoder,
            Clock clock,
            String issuer
    ) {
        this.userRepository = userRepository;
        this.backupCodeRepository = backupCodeRepository;
        this.totpEncryptionService = totpEncryptionService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.issuer = issuer;
    }

    @Transactional
    public TotpSetupResponse setup(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        String secret = new DefaultSecretGenerator(SECRET_BYTES).generate();
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
            String raw = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
            entities.add(TotpBackupCode.create(UUID.randomUUID(), user, passwordEncoder.encode(raw)));
            rawCodes.add(raw);
        }
        backupCodeRepository.saveAll(entities);

        return new TotpSetupResponse(qrData.getUri(), rawCodes);
    }
}