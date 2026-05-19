package com.example.backend.service.auth;

import com.example.backend.exception.RateLimitExceededException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginRateLimiter {

	private static final int MAX_ATTEMPTS = 10;
	private static final Duration WINDOW = Duration.ofMinutes(1);

	private final Clock clock;
	private final ConcurrentMap<String, Window> attemptsByIp = new ConcurrentHashMap<>();

	public LoginRateLimiter() {
		this(Clock.systemUTC());
	}

	LoginRateLimiter(Clock clock) {
		this.clock = clock;
	}

	public void consume(String ipAddress) {
		Instant now = Instant.now(clock);
		String key = normalizeIp(ipAddress);

		Window window = attemptsByIp.compute(key, (ignored, current) -> {
			if (current == null || current.isExpired(now)) {
				return new Window(now, 1);
			}

			return current.incremented();
		});

		if (window.attempts() > MAX_ATTEMPTS) {
			throw new RateLimitExceededException();
		}
	}

	private String normalizeIp(String ipAddress) {
		if (ipAddress == null || ipAddress.isBlank()) {
			return "unknown";
		}

		return ipAddress;
	}

	private record Window(Instant startedAt, int attempts) {

		private boolean isExpired(Instant now) {
			return !startedAt.plus(WINDOW).isAfter(now);
		}

		private Window incremented() {
			return new Window(startedAt, attempts + 1);
		}
	}
}
