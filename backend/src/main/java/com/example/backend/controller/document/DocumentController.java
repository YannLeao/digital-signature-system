package com.example.backend.controller.document;

import com.example.backend.domain.AuditAction;
import com.example.backend.domain.User;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.dto.document.SignedPdfResult;
import com.example.backend.exception.ApiErrorCode;
import com.example.backend.exception.ApiException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.ClientContext;
import com.example.backend.service.audit.AuditService;
import com.example.backend.service.document.PdfSigningService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("documents")
public class DocumentController {

    private final PdfSigningService pdfSigningService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final AuditService auditService;

    public DocumentController(PdfSigningService pdfSigningService,
                              UserRepository userRepository,
                              ObjectMapper objectMapper,
                              Validator validator,
                              AuditService auditService) {
        this.pdfSigningService = pdfSigningService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.auditService = auditService;
    }

    @PostMapping(value = "sign", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> sign(
            @RequestPart("file") MultipartFile file,
            @RequestPart("request") String rawRequest,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest servletRequest) {

        SignDocumentRequest request = parseRequest(rawRequest);
        UUID userId = UUID.fromString(jwt.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario nao encontrado."));

        ClientContext clientContext = new ClientContext(servletRequest.getRemoteAddr(), servletRequest.getHeader("User-Agent"));
        SignedPdfResult result;
        try {
            result = pdfSigningService.sign(file, request, user, clientContext.ipAddress(), Instant.now());
        } catch (RuntimeException exception) {
            auditService.logFailure(userId, AuditAction.DOC_SIGNED, clientContext.ipAddress(), clientContext.userAgent());
            throw exception;
        }

        auditService.logSuccess(
                userId,
                AuditAction.DOC_SIGNED,
                clientContext.ipAddress(),
                clientContext.userAgent(),
                "{\"signatureId\":\"" + result.signatureId() + "\"}"
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signed-document.pdf\"")
                .header("X-Signature-Id", result.signatureId().toString())
                .header("X-Original-Hash", result.originalHash())
                .header("X-Signed-Hash", result.signedHash())
                .header("X-Signed-At", result.signedAt().toString())
                .body(result.pdfBytes());
    }

    private SignDocumentRequest parseRequest(String rawRequest) {
        try {
            SignDocumentRequest request = objectMapper.readValue(rawRequest, SignDocumentRequest.class);
            if (!validator.validate(request).isEmpty()) {
                throw invalidRequest();
            }
            return request;
        } catch (JsonProcessingException exception) {
            throw invalidRequest();
        }
    }

    private ApiException invalidRequest() {
        return new ApiException(ApiErrorCode.VAL_001, HttpStatus.BAD_REQUEST);
    }
}
