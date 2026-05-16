package com.example.backend.controller;

import com.example.backend.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
public class HealthController {

	private final Clock clock;

	public HealthController() {
		this(Clock.systemUTC());
	}

	HealthController(Clock clock) {
		this.clock = clock;
	}

	@GetMapping("/health")
	public HealthResponse health() {
		return new HealthResponse("UP", "v1", Instant.now(clock));
	}
}
