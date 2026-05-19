package com.example.backend.service.auth;

import com.example.backend.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTests {

	@Test
	void rejectsEleventhAttemptFromSameIpWithinOneMinute() {
		LoginRateLimiter rateLimiter = new LoginRateLimiter(
				Clock.fixed(Instant.parse("2026-05-19T12:00:00Z"), ZoneOffset.UTC)
		);

		for (int attempt = 0; attempt < 10; attempt++) {
			rateLimiter.consume("203.0.113.10");
		}

		assertThatThrownBy(() -> rateLimiter.consume("203.0.113.10"))
				.isInstanceOf(RateLimitExceededException.class);
	}
}
