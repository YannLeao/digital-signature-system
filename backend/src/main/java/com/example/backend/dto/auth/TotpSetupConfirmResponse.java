package com.example.backend.dto.auth;

import java.util.List;

public record TotpSetupConfirmResponse(List<String> backupCodes) {
}

