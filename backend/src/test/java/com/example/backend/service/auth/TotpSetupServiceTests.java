package com.example.backend.service.auth;

import com.example.backend.domain.TotpBackupCode;
import com.example.backend.domain.User;
import com.example.backend.dto.auth.TotpSetupConfirmResponse;
import com.example.backend.dto.auth.TotpSetupResponse;
import com.example.backend.repository.TotpBackupCodeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.TotpEncryptionService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TotpSetupServiceTests {

	@Test
	void setupReturnsOtpAuthAndStoresPendingEncryptedSecretWithoutBackupCodes() {
		UserRepository userRepository = mock(UserRepository.class);
		TotpBackupCodeRepository backupCodeRepository = mock(TotpBackupCodeRepository.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		TotpEncryptionService encryptionService = new TotpEncryptionService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(any())).thenReturn("argon2-backup-hash");
		TotpSetupService service = new TotpSetupService(
				userRepository,
				backupCodeRepository,
				encryptionService,
				passwordEncoder,
				Clock.fixed(Instant.parse("2026-05-22T12:01:00Z"), ZoneOffset.UTC),
				new SecureRandom(new byte[] {1, 2, 3, 4}),
				"Projeto Seguranca"
		);

		TotpSetupResponse response = service.setup(user.getId());

		assertThat(response.otpauthUrl()).startsWith("otpauth://totp/");
		assertThat(response.otpauthUrl()).contains("issuer=Projeto%20Seguranca");
		assertThat(response.backupCodes()).isEmpty();
		String secret = response.otpauthUrl().replaceAll(".*secret=([^&]+).*", "$1");
		assertThat(secret).hasSize(32);
		assertThat(user.getTotpSecretEncrypted()).isNotEqualTo(secret);
		assertThat(encryptionService.decrypt(user.getTotpSecretEncrypted())).isEqualTo(secret);
		assertThat(user.isTotpEnabled()).isFalse();

		verify(backupCodeRepository, never()).deleteAllByUserId(user.getId());
		verify(backupCodeRepository, never()).saveAll(any());
	}

	@Test
	void confirmValidCodeEnablesTotpAndReturnsBackupCodes() throws Exception {
		UserRepository userRepository = mock(UserRepository.class);
		TotpBackupCodeRepository backupCodeRepository = mock(TotpBackupCodeRepository.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		TotpEncryptionService encryptionService = new TotpEncryptionService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(passwordEncoder.encode(any())).thenReturn("argon2-backup-hash");
		TotpSetupService service = new TotpSetupService(
				userRepository,
				backupCodeRepository,
				encryptionService,
				passwordEncoder,
				Clock.fixed(Instant.parse("2026-05-22T12:01:00Z"), ZoneOffset.UTC),
				new SecureRandom(new byte[] {1, 2, 3, 4}),
				"Projeto Seguranca"
		);

		TotpSetupResponse setup = service.setup(user.getId());
		String secret = setup.otpauthUrl().replaceAll(".*secret=([^&]+).*", "$1");
		String validCode = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6)
				.generate(secret, System.currentTimeMillis() / 30000);

		TotpSetupConfirmResponse response = service.confirm(user.getId(), validCode);

		ArgumentCaptor<Iterable<TotpBackupCode>> captor = ArgumentCaptor.forClass(Iterable.class);
		verify(backupCodeRepository).saveAll(captor.capture());
		@SuppressWarnings("unchecked")
		List<TotpBackupCode> savedCodes = (List<TotpBackupCode>) captor.getValue();
		assertThat(savedCodes).hasSize(10);
		assertThat(savedCodes).allSatisfy(code -> assertThat(code.getCodeHash()).isEqualTo("argon2-backup-hash"));
		response.backupCodes().forEach(rawCode ->
				assertThat(savedCodes).noneSatisfy(savedCode -> assertThat(savedCode.getCodeHash()).contains(rawCode)));
		verify(backupCodeRepository).deleteAllByUserId(user.getId());
		assertThat(user.isTotpEnabled()).isTrue();
	}

	@Test
	void confirmInvalidCodeDoesNotEnableTotpOrCreateBackupCodes() {
		UserRepository userRepository = mock(UserRepository.class);
		TotpBackupCodeRepository backupCodeRepository = mock(TotpBackupCodeRepository.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		TotpEncryptionService encryptionService = new TotpEncryptionService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		TotpSetupService service = new TotpSetupService(
				userRepository,
				backupCodeRepository,
				encryptionService,
				passwordEncoder,
				Clock.fixed(Instant.parse("2026-05-22T12:01:00Z"), ZoneOffset.UTC),
				new SecureRandom(new byte[] {1, 2, 3, 4}),
				"Projeto Seguranca"
		);

		service.setup(user.getId());

		assertThatThrownBy(() -> service.confirm(user.getId(), "000000"))
				.isInstanceOf(com.example.backend.exception.InvalidTotpException.class);
		assertThat(user.isTotpEnabled()).isFalse();
		verify(backupCodeRepository, never()).deleteAllByUserId(user.getId());
		verify(backupCodeRepository, never()).saveAll(any());
	}

	@Test
	void setupFailsWhenTotpIsAlreadyEnabled() {
		UserRepository userRepository = mock(UserRepository.class);
		TotpBackupCodeRepository backupCodeRepository = mock(TotpBackupCodeRepository.class);
		PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
		TotpEncryptionService encryptionService = new TotpEncryptionService("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
		User user = User.register(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				"password-hash",
				Instant.parse("2026-05-22T12:00:00Z")
		);
		user.enableTotp("encrypted-secret", Instant.parse("2026-05-22T12:01:00Z"));
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		TotpSetupService service = new TotpSetupService(
				userRepository,
				backupCodeRepository,
				encryptionService,
				passwordEncoder,
				Clock.fixed(Instant.parse("2026-05-22T12:01:00Z"), ZoneOffset.UTC),
				new SecureRandom(new byte[] {1, 2, 3, 4}),
				"Projeto Seguranca"
		);

		assertThatThrownBy(() -> service.setup(user.getId()))
				.isInstanceOf(com.example.backend.exception.BusinessException.class)
				.hasMessage("Autenticacao em duas etapas ja esta ativa.");
		verify(backupCodeRepository, never()).deleteAllByUserId(user.getId());
		verify(backupCodeRepository, never()).saveAll(any());
	}
}
