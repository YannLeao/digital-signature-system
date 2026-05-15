package com.example.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EnvFileLoader {

	private EnvFileLoader() {
	}

	public static void load() {
		candidatePaths().stream()
				.filter(Files::isRegularFile)
				.findFirst()
				.ifPresent(EnvFileLoader::loadFile);
	}

	private static List<Path> candidatePaths() {
		Path workingDirectory = Path.of(System.getProperty("user.dir"));

		return List.of(
				workingDirectory.resolve(".env"),
				workingDirectory.resolve("backend").resolve(".env"),
				workingDirectory.getParent() == null
						? workingDirectory.resolve(".env")
						: workingDirectory.getParent().resolve(".env")
		);
	}

	private static void loadFile(Path path) {
		try {
			for (String line : Files.readAllLines(path)) {
				parseLine(line);
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Unable to read .env file: " + path, exception);
		}
	}

	private static void parseLine(String line) {
		String trimmedLine = line.trim();

		if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
			return;
		}

		int separatorIndex = trimmedLine.indexOf('=');

		if (separatorIndex <= 0) {
			throw new IllegalStateException("Invalid .env entry: " + line);
		}

		String key = trimmedLine.substring(0, separatorIndex).trim();
		String value = trimmedLine.substring(separatorIndex + 1).trim();

		if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
			value = value.substring(1, value.length() - 1);
		}

		if (System.getenv(key) == null && System.getProperty(key) == null) {
			System.setProperty(key, value);
		}
	}
}
