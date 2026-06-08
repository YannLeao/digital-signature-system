package com.example.backend.controller.document;

import com.example.backend.domain.User;
import com.example.backend.dto.document.SignDocumentRequest;
import com.example.backend.dto.document.SignDocumentResponse;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.document.PdfSigningService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final UserRepository    userRepository;

    public DocumentController(PdfSigningService pdfSigningService,
                               UserRepository userRepository) {
        this.pdfSigningService = pdfSigningService;
        this.userRepository    = userRepository;
    }

    @PostMapping(value = "sign", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignDocumentResponse> sign(
            @RequestPart("file")    MultipartFile file,
            @RequestPart("request") @Valid SignDocumentRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest servletRequest) {

        UUID userId = UUID.fromString(jwt.getSubject());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        SignDocumentResponse response = pdfSigningService.sign(
                file, request, user,
                servletRequest.getRemoteAddr(),
                Instant.now());

        return ResponseEntity.ok(response);
    }
}