package com.example.backend.service.auth;

import com.example.backend.domain.User;
import com.example.backend.dto.auth.RegisterUserRequest;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.signature.UserKeyService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRegistrationServiceTests {

    private static final Instant NOW   = Instant.parse("2026-05-19T12:00:00Z");
    private static final Clock   CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    private final UserRepository          userRepository  = mock(UserRepository.class);
    private final PasswordEncoder         passwordEncoder = new Argon2PasswordEncoder(16, 32, 4, 65536, 3);
    private final UserKeyService          userKeyService  = mock(UserKeyService.class);
    private final UserRegistrationService service         =
            new UserRegistrationService(userRepository, passwordEncoder, userKeyService, CLOCK);

    @Test
    void savesNormalizedEmailAndArgon2idHashOnly() {
        String plainPassword = "StrongPassword123!";
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);

        service.register(new RegisterUserRequest(" User@Example.COM ", plainPassword));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("user@example.com");
        assertThat(savedUser.getPasswordHash()).startsWith("$argon2id$");
        assertThat(savedUser.getPasswordHash()).doesNotContain(plainPassword);
        assertThat(passwordEncoder.matches(plainPassword, savedUser.getPasswordHash())).isTrue();
        assertThat(savedUser.getCreatedAt()).isEqualTo(NOW);
        assertThat(savedUser.getUpdatedAt()).isEqualTo(NOW);
        assertThat(savedUser.getFailedAttempts()).isZero();
        assertThat(savedUser.getLockedUntil()).isNull();
        verify(userKeyService).generateAndStoreKeyPair(savedUser, NOW);
    }

    @Test
    void rejectsDuplicateEmailWithControlledBusinessError() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterUserRequest("user@example.com", "StrongPassword123!")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail ja cadastrado.");

        verify(userRepository, never()).save(any(User.class));
        verify(userKeyService, never()).generateAndStoreKeyPair(any(), any());
    }

    @Test
    void convertsDatabaseUniqueViolationToControlledDuplicateEmailError() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> service.register(new RegisterUserRequest("user@example.com", "StrongPassword123!")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("E-mail ja cadastrado.");
        verify(userKeyService, never()).generateAndStoreKeyPair(any(), any());
    }
}
