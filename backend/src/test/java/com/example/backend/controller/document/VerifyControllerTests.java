package com.example.backend.controller.document;

import com.example.backend.dto.document.VerifyDocumentResponse;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.document.PdfVerificationService;
import com.example.backend.service.document.VerifyRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class VerifyControllerTests {

    private final PdfVerificationService verificationService = mock(PdfVerificationService.class);
    private final VerifyRateLimiter rateLimiter = new VerifyRateLimiter();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new VerifyController(verificationService, rateLimiter))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void publicUploadReturnsVerificationResultWithoutAuthentication() throws Exception {
        when(verificationService.verify(any(), any())).thenReturn(VerifyDocumentResponse.notFound());

        mockMvc.perform(multipart("/verify")
                        .file(pdfFile())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void eleventhVerificationFromSameIpReturnsTooManyRequests() throws Exception {
        when(verificationService.verify(any(), any())).thenReturn(VerifyDocumentResponse.notFound());

        for (int attempt = 0; attempt < 10; attempt++) {
            mockMvc.perform(multipart("/verify")
                            .file(pdfFile())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(multipart("/verify")
                        .file(pdfFile())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("SEC_001"));
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile("file", "document.pdf", "application/pdf", "%PDF-".getBytes());
    }
}
