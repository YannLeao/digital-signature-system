package com.example.backend.listener;

import com.example.backend.event.NewLoginEvent;
import com.example.backend.event.PasswordChangedEvent;
import com.example.backend.event.TwoFactorChangedEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailNotificationListenerTests {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailNotificationListener listener = new EmailNotificationListener(mailSender, "no-reply@example.com");

    @Test
    void sendsSecurityEmailsForRequirementEvents() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage());

        listener.onNewLogin(new NewLoginEvent(userId(), "user@example.com", "203.0.113.10", Instant.parse("2026-06-18T12:00:00Z")));
        listener.onTwoFactorChanged(new TwoFactorChangedEvent(userId(), "user@example.com", true, "203.0.113.10", Instant.parse("2026-06-18T12:01:00Z")));
        listener.onPasswordChanged(new PasswordChangedEvent(userId(), "user@example.com", "203.0.113.10", Instant.parse("2026-06-18T12:02:00Z")));

        verify(mailSender, org.mockito.Mockito.times(3)).send(any(MimeMessage.class));
    }

    @Test
    void mailFailureDoesNotBreakSecurityFlow() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage());
        doThrow(new MailSendException("smtp unavailable")).when(mailSender).send(any(MimeMessage.class));

        assertThatCode(() -> listener.onPasswordChanged(new PasswordChangedEvent(
                userId(),
                "user@example.com",
                "203.0.113.10",
                Instant.parse("2026-06-18T12:02:00Z")
        ))).doesNotThrowAnyException();
    }

    private MimeMessage mimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private UUID userId() {
        return UUID.fromString("11111111-1111-1111-1111-111111111111");
    }
}
