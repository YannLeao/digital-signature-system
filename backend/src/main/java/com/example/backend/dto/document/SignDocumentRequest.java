package com.example.backend.dto.document;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SignDocumentRequest(

        @NotNull(message = "Página do selo é obrigatória.")
        @Min(value = 1, message = "Página deve ser maior que 0.")
        Integer sealPage,

        @NotNull(message = "Coordenada X do selo é obrigatória.")
        @Min(value = 0) @Max(value = 10000)
        BigDecimal sealX,

        @NotNull(message = "Coordenada Y do selo é obrigatória.")
        @Min(value = 0) @Max(value = 10000)
        BigDecimal sealY
) {}