package com.example.backend.controller.document;

import com.example.backend.dto.document.VerifyDocumentResponse;
import com.example.backend.security.ClientContext;
import com.example.backend.service.document.PdfVerificationService;
import com.example.backend.service.document.VerifyRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("verify")
public class VerifyController {

    private final PdfVerificationService pdfVerificationService;
    private final VerifyRateLimiter verifyRateLimiter;

    public VerifyController(PdfVerificationService pdfVerificationService,
                            VerifyRateLimiter verifyRateLimiter) {
        this.pdfVerificationService = pdfVerificationService;
        this.verifyRateLimiter = verifyRateLimiter;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VerifyDocumentResponse verify(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request
    ) {
        verifyRateLimiter.consume(request.getRemoteAddr());
        return pdfVerificationService.verify(file, new ClientContext(request.getRemoteAddr(), request.getHeader("User-Agent")));
    }
}
