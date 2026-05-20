package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasskeyStartRequest {
    @NotBlank
    @Email
    private String email;
}
