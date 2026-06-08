package com.example.backend.listener;

import com.example.backend.event.TotpLockedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class TotpLockedEmailListenerTests {

	@Test
	void mailFailureDoesNotBreakAuthenticationFlow() {
		JavaMailSender mailSender = mock(JavaMailSender.class);
		doThrow(new MailSendException("smtp unavailable")).when(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
		TotpLockedEmailListener listener = new TotpLockedEmailListener(mailSender, "no-reply@example.com");

		assertThatCode(() -> listener.onTotpLocked(new TotpLockedEvent(
				UUID.fromString("11111111-1111-1111-1111-111111111111"),
				"user@example.com",
				Instant.parse("2026-05-22T12:15:00Z")
		))).doesNotThrowAnyException();
	}
}
