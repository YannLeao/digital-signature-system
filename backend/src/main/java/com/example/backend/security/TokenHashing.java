package com.example.backend.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class TokenHashing {

	public String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(normalize(value).getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (Exception exception) {
			throw new IllegalStateException("Unable to hash token data.", exception);
		}
	}

	private String normalize(String value) {
		if (value == null) {
			return "";
		}

		return value.trim();
	}
}
