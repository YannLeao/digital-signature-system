package com.example.backend.service.document;

import com.example.backend.exception.PdfValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PdfSandboxService {

    private static final String ERROR_CODE = "DOC_001";
    private static final String VALIDATION_ERROR_PREFIX = "PDF_VALIDATION_ERROR:";

    private final boolean enabled;
    private final Duration timeout;
    private final String javaExecutable;
    private final String classPath;

    @Autowired
    public PdfSandboxService(
            @Value("${PDF_SANDBOX_ENABLED:true}") boolean enabled,
            @Value("${PDF_SANDBOX_TIMEOUT_MS:5000}") long timeoutMillis
    ) {
        this(
                enabled,
                Duration.ofMillis(timeoutMillis),
                defaultJavaExecutable(),
                System.getProperty("java.class.path")
        );
    }

    PdfSandboxService(boolean enabled, Duration timeout, String javaExecutable, String classPath) {
        this.enabled = enabled;
        this.timeout = timeout;
        this.javaExecutable = javaExecutable;
        this.classPath = classPath;
    }

    public void validateStructure(byte[] bytes) {
        if (!enabled) {
            PdfValidationCore.validateStructure(bytes);
            return;
        }

        Process process = startProcess();
        try {
            process.getOutputStream().write(bytes);
            process.getOutputStream().close();

            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw invalid("Tempo limite excedido no sandbox de validacao de PDF.");
            }

            if (process.exitValue() == 0) {
                return;
            }

            throw invalid(errorMessage(process.getErrorStream().readAllBytes()));
        } catch (PdfValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            process.destroyForcibly();
            throw invalid("Nao foi possivel processar o PDF no sandbox de validacao.");
        } finally {
            process.destroy();
        }
    }

    private Process startProcess() {
        List<String> command = List.of(
                javaExecutable,
                "-Xmx128m",
                "-Djava.awt.headless=true",
                "-cp",
                classPath,
                PdfSandboxWorker.class.getName()
        );

        try {
            return new ProcessBuilder(command).start();
        } catch (IOException exception) {
            throw invalid("Nao foi possivel iniciar o sandbox de validacao de PDF.");
        }
    }

    private String errorMessage(byte[] stderr) {
        String error = new String(stderr, StandardCharsets.UTF_8).trim();
        if (!error.startsWith(VALIDATION_ERROR_PREFIX)) {
            return "Nao foi possivel processar o PDF no sandbox de validacao.";
        }

        try {
            String encoded = error.substring(VALIDATION_ERROR_PREFIX.length());
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return "Nao foi possivel processar o PDF no sandbox de validacao.";
        }
    }

    private PdfValidationException invalid(String message) {
        return new PdfValidationException(ERROR_CODE, message);
    }

    private static String defaultJavaExecutable() {
        String executable = System.getProperty("os.name", "").toLowerCase().contains("win")
                ? "java.exe"
                : "java";
        return Path.of(System.getProperty("java.home"), "bin", executable).toString();
    }
}
